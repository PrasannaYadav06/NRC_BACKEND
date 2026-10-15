package com.hfw.nrc.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

/**
 * Image captcha for /login, modeled on KABHI_BACKEND's flow (CaptchaController
 * + inline verification in KabhiUsersController): generate a distorted-text
 * image server-side, hand the client an opaque id + the image, verify the
 * typed answer against the id at login and delete it immediately after (one
 * use only, anti-replay). KABHI backs this with Redis; NRC_BACKEND has no
 * Redis dependency and this is a single-instance local deployment, so an
 * in-memory store (with the same short TTL) is the equivalent here - see
 * KABHI's own in-code comment flagging its 600s TTL as "TEMP, revert before
 * deploy"; this uses a real 120s TTL from the start.
 *
 * No new dependency added (no kaptcha/jcaptcha library) - plain java.awt/
 * ImageIO, which works headless (no display needed) for pure image
 * generation.
 */
@Component
public class CaptchaService {

	private static final int TTL_MS = 120_000;
	private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I - avoids ambiguity
	private static final int LENGTH = 5;
	private static final int WIDTH = 160;
	private static final int HEIGHT = 60;

	private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();
	private final Random random = new Random();

	private static class Entry {
		final String answer;
		final long expiresAt;

		Entry(String answer, long expiresAt) {
			this.answer = answer;
			this.expiresAt = expiresAt;
		}
	}

	public static class Captcha {
		public final String captchaId;
		public final String imageDataUrl;
		/** Only set when nrc.captcha.debug-expose-answer=true (application-local.properties, never committed/shared) - lets local dev/automated testing read the answer without OCR-ing the rendered image. Null in every other environment. */
		public final String debugAnswer;

		Captcha(String captchaId, String imageDataUrl, String debugAnswer) {
			this.captchaId = captchaId;
			this.imageDataUrl = imageDataUrl;
			this.debugAnswer = debugAnswer;
		}
	}

	@org.springframework.beans.factory.annotation.Value("${nrc.captcha.debug-expose-answer:false}")
	private boolean debugExposeAnswer;

	public Captcha generate() {
		purgeExpired();

		StringBuilder sb = new StringBuilder(LENGTH);
		for (int i = 0; i < LENGTH; i++) {
			sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
		}
		String text = sb.toString();
		String id = UUID.randomUUID().toString();
		store.put(id, new Entry(text, System.currentTimeMillis() + TTL_MS));

		return new Captcha(id, "data:image/png;base64," + renderBase64(text), debugExposeAnswer ? text : null);
	}

	/** One-shot: the id is removed whether the answer matches or not (anti-replay). */
	public boolean verify(String captchaId, String answer) {
		if (captchaId == null || answer == null) {
			return false;
		}
		Entry entry = store.remove(captchaId);
		if (entry == null || System.currentTimeMillis() > entry.expiresAt) {
			return false;
		}
		return entry.answer.equalsIgnoreCase(answer.trim());
	}

	private void purgeExpired() {
		long now = System.currentTimeMillis();
		store.entrySet().removeIf(e -> now > e.getValue().expiresAt);
	}

	private String renderBase64(String text) {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		// noise lines
		for (int i = 0; i < 6; i++) {
			g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
			g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), random.nextInt(WIDTH), random.nextInt(HEIGHT));
		}

		g.setFont(new Font("Serif", Font.BOLD, 32));
		int x = 15;
		for (char c : text.toCharArray()) {
			g.setColor(new Color(20 + random.nextInt(100), 20 + random.nextInt(100), 20 + random.nextInt(100)));
			int y = 40 + random.nextInt(10) - 5;
			double angle = (random.nextDouble() - 0.5) * 0.5;
			g.rotate(angle, x, y);
			g.drawString(String.valueOf(c), x, y);
			g.rotate(-angle, x, y);
			x += 26;
		}

		// noise dots
		for (int i = 0; i < 60; i++) {
			g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
			g.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
		}

		g.dispose();

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		} catch (IOException e) {
			throw new IllegalStateException("Failed to render captcha image", e);
		}
	}
}
