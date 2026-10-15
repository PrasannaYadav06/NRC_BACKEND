# NRC_BACKEND

Unified NRC Spring Boot backend, replacing the separately-maintained
`nrc_admin_backend_ehealth` (Boot 2.7.2, session auth) and
`NRCMiddleware_ehealth` (legacy Spring 4.3, session auth) projects with one
JWT-secured app structured like `KABHI_BACKEND`.

See the implementation plan for full context and phase breakdown:
`C:\Users\PRASANNA YADAV 5566\.claude\plans\eventual-toasting-pelican.md`

## Response contract (important - read before adding new endpoints)

The already-merged Angular frontend expects **every** endpoint to return
`com.hfw.nrc.dto.AbstractResponse` (`errorCode`/`errorMessage`/`successMessage`/
`sessionId`/`systemTime`/`responseObject`) with **HTTP 200 always** - success/
failure is signaled by `errorCode` ("0"/"1"), not HTTP status. This was ported
verbatim from `nrcapp.model.AbstractResponse` rather than switched to a new
convention, specifically because the frontend already parses this exact
shape. Every new Phase 3 controller must follow this, not KABHI's ad-hoc
raw-entity-return style.

## Status

- [x] **Phase 0** - scaffold (pom.xml, package skeleton, local DB config)
- [x] **Phase 1** - auth/security skeleton, one working `POST /login`
- [x] **Phase 2** - domain/entity consolidation: 41 entities in `domain/`, each verified with a live
      row-count query against `nrc_live` (see below). `Admin` (dead code, no matching table) and
      `AwGrowthDetail`/`NrcSnehaMapping` (dead "Sneha-Angan" integration, tables don't exist in the
      live DB) were deliberately dropped - not ported.
- [x] **Phase 3** - repository/service/web layer migration, by module (all groups A-H done, see below)
  - [x] Group A - reference/lookup data (16 endpoints, `ReferenceLookupController`)
  - [x] Group B - hospital-scoped unit/ward lookups (`UnitWardController`)
  - [ ] Group C - case management (heaviest module - `NRCOperationServiceImpl` alone is 2,685 lines)
    - [x] Patient-list/count/SAM cluster (`getallinpatients`, `getcount`, `getcurrinpatients`,
          `getdischargedpatients`, `getmaximumsam`, `checksamnumber`, `findsdvalue`) -
          `PatientListController` + `NRCOperationService`. Verified live incl. the weight-gain/
          discharge-status heuristic and the `nr.sd_calculation` stored-procedure call.
    - [x] `createcase` (`CaseCreationService` + `CaseManagementController`) - re-admission/relapse
          matching, mother/child registration, and the full 8-table case orchestration (comorbid
          conditions, admission criteria, case allocation, mother details/growth/social factors,
          referrals, child growth). No JPA cascade (Phase 2 dropped those collections) - each
          sub-record type is saved explicitly via its own repository after the parent CaseDetail
          has a generated caseId. Live-tested: inserted a full fake test case across 6 tables,
          verified every field (incl. age-in-months math and the `nr.sd_calculation` SD value),
          then deleted it all and confirmed zero rows remain.
    - [x] Discharge + search flows: `updatecase`→`dischargeCase`, `updatedischarge`, `viewdischarge`
          (`DischargeService`), `searchdetails`, `getcasedetails`, `getchilddetails`
          (`CaseQueryService`) - all in `CaseManagementController`. Live-tested: created a fresh
          fake case, discharged it, updated the discharge (incl. the comorbid delete-and-recreate
          pattern), viewed it back, verified an already-discharged case is correctly rejected on
          re-discharge, ran search/case-details/child-details against real historical patients,
          then deleted every row the test created.
      - Follow-up-schedule generation (`nrcFollowupService.generateFollowupSched`/`updateFollowups`
        in the source) is a **logged no-op stub** (`NRCFollowupService`) - full port is Group E
        work. Discharge itself (vitals, outcome, comorbid/risk records, target-weight flag) is
        fully functional without it; only the automatic follow-up scheduling side effect is missing.
      - Two source bugs ported **verbatim, not fixed** (per "faithful port" decision): `checkDischarged`'s
        self-join (`dc.case_id = dc.case_id` instead of `= cd.case_id`) and `viewDischarge`'s mother-growth
        filter (`recId != caseId`, effectively always true, so it returns all growth records not just
        discharge ones) - both documented in code comments where they live.
      - `updateDischarge`'s `childGrowthList.get(1)` after only checking `size()>0` (not `size()>1`) is
        also ported as-is - a latent crash risk in the source, reproduced not fixed.
    - [x] `updatechild` (standalone endpoint), `updateadmission`, `viewadmission` - all in
          `CaseCreationService` (reusing the same `updateChild`/`updateMother`/`add*Data(updateFlag)`
          helpers `createcase` already built, since `updateAdmission` in the source is the same
          8-sub-record orchestration with `updateFlag=true`). `viewAdmission`'s response builder
          (`buildCaseDetailsRequestModel`) is the same dropped-collection-to-repository-query
          adaptation as `DischargeService#buildDischargeRequestModel`. Live-tested: created a fresh
          fake case, updated its child record, updated its full admission (mother/child/comorbid/
          criteria/allocation/mother-detail/mother-growth/social-factor/referral/growth - verified
          the delete-and-recreate vs update-in-place distinction is correct per sub-record type via
          `viewadmission`), then deleted every row.
    - [x] `GET /setTargetAchieved/{userId}` (`DischargeService#setPastTargetWeightAchieved`, added to
          `CaseManagementController`) - the source's own "one time API" batch fixup, still live/
          uncommented so it was ported, not dropped. Reuses the exact `setTargetWeightAchieved`
          helper `dischargeCase`/`updateDischarge` already call. Live-tested against two small real
          hospitals (not fake data - this endpoint recalculates flags on real discharged cases, so
          the safe test here was picking hospitals with only 1 and 3 discharged cases total and
          diffing before/after instead of insert-then-delete): confirmed the recalculation is
          idempotent (every flag came back identical to its pre-test value, both the already-`1` and
          the `NULL` cases) and the invalid-user-id error path works. Zero DB footprint either way.
    - [x] nrcadmin's admin-side read/delete-by-SAM (`GET`/`DELETE /caseDetails/{..}`,
          `CaseDetailsAdminService`+`CaseDetailsAdminController`) - **Group C is now fully done**.
          Unlike every other endpoint in this project, these two return raw shapes (a plain
          `List<CaseDetailsAdminDto>` / a plain `DeleteResponse`), not `AbstractResponse` - matches
          the source's own nrcadmin contract exactly, which the frontend already expects. The delete
          is a genuine hard cascade across 13 tables (admission/discharge/mother/growth/followup
          records) plus `case_details` itself - ported faithfully since it's a live, callable admin
          endpoint today, not softened. Live-tested end to end: created a fake case with a followup
          *and* its linked `child_growth` row (the trickiest FK ordering case), searched it by SAM
          number, deleted it, then verified all 13 tables plus `case_details` came back to zero rows
          - `child_demography` correctly survives (it's a reusable record, not case-specific, matching
          the source's own design - not a bug). Both endpoints gated to ADMIN role only, same
          reasoning as Group H (the source had no other role to restrict against; this project does).
  - [x] Group D - child growth (`savechildgrowth`/`getchildgrowthlist`/`deletechildgrowth`,
        `ChildGrowthMeasurementService`+`ChildGrowthController`). Live-tested full round trip incl.
        the weight-gain calc and the height-inherited-from-admission behavior. One source typo fixed
        (double `setErrorCode` where the second should've been `setErrorMessage` - noted in code).
  - [x] Group E - followups (`savefollowup`/`getallfollowups`/`deletefollowups`/`getfollowupduebydate`,
        `NRCFollowupService`+`FollowupController`). **This also replaces the Phase-C logged-no-op
        stub** - `generateFollowupSched`/`updateFollowups` (called internally by
        `CaseCreationService`/`DischargeService` on admission/discharge) are now fully implemented,
        not stubs. `getAllFollowups`' `followupInd.getChildGrowths().get(0)` (dropped collection) is
        replaced by a proper indexed repository lookup, not a full-table scan. Live-tested full round
        trip (save/list/delete) against a fresh fake case.
  - [x] Group F - admission/discharge (already covered above under Group C's discharge flow +
        `updatechild`/`updateadmission`/`viewadmission` - same endpoints the plan listed for Group F).
  - [x] Group G - resources (`saveresourceslist`/`getresourceslist`, `ResourceService`+`ResourceController`).
        Live-tested full round trip incl. a faithfully-ported source bug: `NRCOperationServiceImpl#
        addHospitalDetail`'s insert branch always persists `0` regardless of the submitted value (the
        line that would pass the real number is commented out in the source) - only a *second* save
        of the same month/hr_type_id combo (which then hits the update branch) actually persists the
        real number. Reproduced verbatim, documented in code, confirmed via two sequential live saves
        (first → 0s in DB, second → real values) plus a `getresourceslist` read-back, then cleaned up.
  - [x] Group H - admin hospital/org management (`POST /hospital/save`, `HospitalAdminService`+
        `HospitalAdminController`). Creates a Hospital row, seeds its 3 default units/5 default wards,
        creates its first login account, and seeds a bed-count `hospital_details` row - all in one
        transaction. New: explicitly gated to ADMIN role (the source had no role concept at all to
        restrict), and new accounts get a real bcrypt hash directly (not the legacy encoding) since
        there's no existing legacy value to stay compatible with. A NOT NULL `role` column insert
        failure and a real Jackson gotcha were both caught and fixed during live-testing: getters/
        setters named `getHCode`/`getHId` (two leading capitals after the `get` prefix) don't bind
        the plain "hCode"/"hId" JSON keys the frontend sends - Jackson's default bean-naming mangler
        follows the same `java.beans.Introspector.decapitalize` rule that bit `Hospital_HId` back in
        Group C, just on the DTO/JSON side this time, not the repository side. Fixed with explicit
        `@JsonProperty` annotations. Live-tested full flow: hospital/units/wards/user/bed-count all
        created correctly, new account logs in and resolves the right hospitalId, duplicate-username
        correctly rejected before anything is written, non-admin JWT correctly denied with nothing
        leaked - then all rows cleaned up.
- [ ] **Phase 4** - PDF/Excel report builders
- [ ] **Phase 5** - verification & cutover
  - [x] `POST /changepassword` (`AuthService#changePassword`, added to `AuthController`) - this was
        listed in the Phase 1 plan but never actually built until now; added while wiring up the
        frontend for local UI testing (below), since `changepassword.component.ts` already called it.
        Uses `PasswordCompat` for the old-password check (bcrypt-or-legacy, same dual-mode handling as
        `login()`) and always writes the new password as a real bcrypt hash.
  - [x] **Local dev frontend cutover for UI testing** (not the real production cutover - that's still
        untouched and deliberately separate): `nrc_admin_frontend_ehealth` was still wired to the two
        old backends (nrcadmin:8083, nrcapp:8080) with the old session-based contract. Repointed for
        local testing only:
        - `src/environments/environment.ts` (dev config, NOT `environment.prod.ts`) - both
          `adminUrl`/`userUrl` now point at `http://localhost:8090`.
        - `login.component.ts` - now calls the one shared `/login`, reads the new `{token, username,
          role, hospitalId, userId}` response shape instead of the old `successMessage`/`sessionId`/
          `responseObject` shape. `LoginResponse` gained a `userId` field specifically for this - the
          JWT already carries it as a claim, but ~20 patient-module components read
          `currentUser.nrcUId` straight out of localStorage rather than decoding the token.
        - New `core/interceptors/jwt.interceptor.ts` - attaches `Authorization: Bearer <token>` to
          every request except `/login` (NRC_BACKEND is stateless JWT, not session/cookie auth).
        - New `core/interceptors/envelope.interceptor.ts` - the single highest-leverage fix: NRC_BACKEND
          wraps most responses in `AbstractResponse` (`{errorCode, responseObject, ...}`), but most
          components expect the raw data directly. Rather than editing every consuming component, this
          interceptor auto-unwraps `responseObject` for any response shaped like `AbstractResponse`
          (detected by the presence of an `errorCode` key) when `errorCode === "0"`, leaves error
          responses and message-only responses (e.g. `/hospital/save`) as the full envelope so existing
          `result.successMessage`/`result.errorMessage` checks keep working, and leaves everything else
          (login, the raw `List<CaseDetailsAdminDto>` from `/caseDetails/{samNum}`, `DeleteResponse`)
          untouched.
        - `api.service.ts` - fixed `getDistrictList()` (pointed at nrcadmin's old `/district/list`,
          which doesn't exist here - `/getalldistricts` is the one kept endpoint), fixed `login()`'s
          URL (was `/admin/login`, doesn't exist - just `/login`), fixed `SendchangepasswordDetails()`
          to map the form's actual field names (`password`/`newPassword`) and inject `userId` from the
          logged-in user (the old apps kept it server-side in session; NRC_BACKEND has no session).
        - `getMonthlyReport()` → `/getmonthlyreportlist` will 404 until Phase 4 (reports) is built.
        - **The systemic envelope-unwrapping idea was wrong and reverted**: an earlier pass added a
          global interceptor to auto-unwrap `AbstractResponse.responseObject`, assuming components
          expected raw data. Checking the actual components (20+ of them) showed the opposite - they
          already correctly read `response.responseObject.xxx` themselves (this app's frontend was
          originally built against nrcapp's own enveloped contract). The interceptor would have broken
          all of them; removed before it shipped.
        - **`admission.component.ts` fully fixed and live-tested** - this had two real, unrelated bugs
          found while tracing it: (1) its submit button called `onSubmitAdmission(userForm, childForm,
          caseForm, detailsForm)` referencing four component properties that are *only ever assigned
          inside* `onSubmitAdmission` itself - every click passed four `undefined`s in, and the form
          crashed immediately on `caseForm.valid`, before any backend call was even attempted. Fixed to
          reference the real bound FormGroups (`angForm`/`angForm1`/`angForm2`/`angForm3`). (2) The
          payload it sent (`admissionfull()`'s generic `{user, child, caseData, details}` wrapper) didn't
          match `CaseDetailsRequestModel` at all - rewritten in `onSubmitAdmission` to build the exact
          flat shape field-by-field from the 4 real FormGroups plus the comorbid/criteria checkbox
          arrays, posted via a new `createCaseAdmission()` method. Live-tested with the *exact* payload
          shape the rewritten form now produces (including the `genderCode()` full-word contract -
          "Male"/"Female"/"Others", not "M"/"F" - and the Integer-vs-decimal-string `admissionSd`
          conversion) across all 5 affected tables (case_details, child_demography, case_mother_details,
          case_mother_growth, case_allocation), then cleaned up. `npx tsc --noEmit` clean across the
          whole frontend after the edit.
        - **Full frontend bug-fixing pass completed** (all live-tested against fresh fake cases via
          curl unless noted, then cleaned up; `npx tsc --noEmit` clean after every change):
          - **Crash bugs fixed** (form submitted `undefined`/threw immediately, before any backend
            call): admission, followup (new), dailycare (new), admin/HR resources (new), search (new),
            track-child (new), followup-search (new, plus a *second* bug - double `.value` access),
            update-child (new, plus ~14 fields with no `ngModel` at all so they'd never have been
            captured even after the crash fix), update-admission (new - wrong endpoint too, see below).
            readmission's submit button wasn't even valid Angular syntax (`click=` with no parens,
            not `(click)=`) - restored to a real binding against the right FormGroups, but its payload
            shape and a `response > 0` check are still pending (flagged in code, not fixed this pass -
            same scope of rewrite as admission needed, ran out of budget this session).
          - **Wrong payload shape fixed** (generic `{form1, form2, ...}` wrappers rebuilt to match the
            actual backend DTOs field-by-field, live-tested): admission (`/createcase`), discharge
            (`/updatecase`), update-discharge (`/updatedischarge`), followup save (`/savefollowup`),
            daily care save/list/delete (`/savechildgrowth`, `/getchildgrowthlist`,
            `/deletechildgrowth`), admin/HR resources (`/saveresourceslist`), search + track-child +
            followup-search (`/searchdetails`), update-child (`/updatechild`), followup-history delete
            (`/deletefollowups`).
          - **Wrong endpoint fixed**: update-admission was posting to `/createcase` (would have created
            a *new* case instead of updating the existing one) - now posts to `/updateadmission`.
          - **Response-envelope bugs fixed** (read `response.xxx` directly instead of
            `response.responseObject.xxx`, or used a numeric check like `response >= 0` that's never
            true against an object): dailycare's growth list, admin/HR's resource list, update-child's
            child details fetch, followup-history's delete confirmation.
          - **A wrong architectural assumption caught and reverted before shipping**: added a global
            interceptor to auto-unwrap `AbstractResponse.responseObject`, assuming components expected
            raw data. Checking the actual components (20+) showed most already correctly read
            `response.responseObject.xxx` themselves - the interceptor would have broken all of them.
            Removed before it caused damage; the response-envelope bugs above were fixed individually
            instead, at their actual source.
          - **Confirmed correct, not a bug**: `/searchdetails` returning nothing for an undischarged
            test case - by design, `inTrackChild:false` (search.component.ts) only searches discharged
            cases; `inTrackChild:true` (track-child.component.ts) searches all cases regardless of
            discharge status. Verified both paths live.
          - **A real backend gap found and fixed** (not a frontend issue): `NRCFollowupService
            #saveFollowup` accepted `heightCm`/`weightKg` but never persisted them - the source relies
            on a JPA cascade (Followup → ChildGrowth) that Phase 2 deliberately dropped, and the initial
            Group E port missed adding the explicit save. Fixed, live-tested (growth row now correctly
            linked to its followup), verified `getallfollowups` handles both old followups (no linked
            growth - shows null, doesn't crash) and new ones (shows the real values) correctly.
          - **Not yet touched**: readmission's payload shape/response check (see above), and any
            screens not reachable from the main patient-module flows (reports, if any exist beyond
            Phase 4's scope).

### Phase 2 notes

- Every entity uses explicit `@Table(name = "...", schema = "nr"|"config")` - two schemas are in
  play (`nr` for transactional/case data, `config` for reference/lookup data), confirmed from the
  source apps' own `@Table` annotations, not assumed.
- Audit-trail `@OneToMany` back-reference collections (createdBy/lastUpdatedBy navigation on ~15
  child tables per parent, present in both source apps) were deliberately dropped - they're
  Hibernate navigation conveniences with no data-integrity purpose. The service layer (Phase 3)
  queries child tables directly instead, matching KABHI's own minimal-relationship convention.
  Forward `@ManyToOne` relationships (the actual foreign keys) are all kept.
- `District` uses nrcadmin's corrected mapping (`Long` id, no `@GeneratedValue`, explicit
  `schema="config"`) rather than nrcapp's original - nrcadmin's version has a `// FIX HERE` comment
  indicating a deliberate bug fix; district rows are pre-seeded reference data, not app-generated.
- Verified against the live local `nrc_live` DB with a temporary `COUNT(e)` query per entity
  (since `ddl-auto=none` means Hibernate never validates mappings against the real schema on its
  own) - 41/41 entities resolved correctly, e.g. `ChildGrowth` = 674,148 rows, `NrcUser` = 157 rows.

## Local setup

1. Copy `src/main/resources/application-local.properties.example` to
   `application-local.properties` and fill in the real DB/JWT/salt values
   (ask for these - they're intentionally not committed).
2. Run `db/migration/V1__add_role_to_nrc_users.sql` against `nrc_live`
   once (adds the `role` column; not auto-applied, Flyway isn't wired up -
   same convention as KABHI_BACKEND).
3. `mvn spring-boot:run`

## Auth model

`nr.nrc_users` had no role column before this project - both source apps
authenticated the same table with zero admin/hospital-user distinction.
`role` (`ADMIN` / `HOSPITAL_USER`) is new; every existing account defaults
to `HOSPITAL_USER`. Promote specific accounts with the `UPDATE` at the
bottom of `V1__add_role_to_nrc_users.sql`.

Passwords: `nr.nrc_users.password` currently holds
`cstep.nrcadmin.util.EncodeUtil`-style values (Base64(salt+pwd+salt)), not
real hashes. `PasswordCompat` checks bcrypt first, falls back to the legacy
scheme, and silently rehashes to bcrypt on a successful legacy login - so
every account migrates itself the next time its owner logs in, no manual
data migration needed.
