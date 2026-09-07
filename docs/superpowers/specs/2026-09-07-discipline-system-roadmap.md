# TradeTrack — Discipline System Roadmap

**Date:** 2026-09-07
**Status:** Direction agreed in brainstorming; individual pillars not yet specced
**Branch:** `docs/discipline-system-planning`
**Companion spec:** [`2026-09-07-discipline-checklist-design.md`](./2026-09-07-discipline-checklist-design.md)

---

## 1. Thesis

The competitor teardown of tradediary.in showed they compete on *"advanced analytics"* and a
large programmatic-SEO surface. Matching them on analytics is low-value — it is not where
traders actually lose money. Traders lose money on **process and discipline**.

TradeTrack's opportunity is to own that ground:

> **TradeTrack is a discipline system, not just a journal.**
> Plan the day → trade the plan → check yourself → review the week.

This roadmap covers the five pillars that deliver that loop. Pillar 1 (the discipline
checklist) is specced in the companion document. Pillars 2–5 are the "Tier 1" features agreed
in brainstorming: each is described here at roadmap depth and **needs its own
brainstorming + spec pass** before implementation.

Every pillar is chosen to also exploit a structural advantage TradeTrack has and tradediary.in
does not: **a native Android app that works offline**.

## 2. The loop

| Stage | Pillar | What the trader does |
|---|---|---|
| Plan | **1. Discipline checklist** (pre-market) | Ticks a pre-market routine before the open. |
| Trade | **3. Trading plan / risk rules** | Trades inside daily loss / trade-count / risk limits; violations get flagged. |
| Check | **1. Discipline checklist** (post-market) + **2. Nudges** | Post-market review prompt; app reminds if it wasn't done. |
| Review | **4. Guided weekly review** | Structured close-the-week ritual, saved as a journal entry. |
| Import | **5. CSV / tradebook import** | Brings real trade history in so the loop has data from day one. |

Pillars 2–5 reinforce Pillar 1: the discipline score defined in the checklist spec already
blends checklist completion with mistake-free trades and stop-loss adherence — the trading-plan
pillar feeds rule-adherence into the same score, and the weekly review consumes it.

## 3. Pillars

### Pillar 1 — Discipline checklist  *(specced)*
See the companion document. Daily pre-market + post-market checklist, editable template,
blended discipline score with a 30-day trend, surfaced via a Dashboard card. Offline-first Room
+ Firestore sync. AI insights panel designed-for but not built.

---

### Pillar 2 — Notifications & nudges

**Problem.** A journal only works if it is actually filled in. The single biggest failure mode
is a trader who logs for a week and then stops. tradediary.in is a website and cannot solve
this well — no reliable scheduled reminders, no lock-screen presence.

**Value.**
- *PM:* highest-leverage retention lever available; directly raises the "logged ≥1 trade this
  week" and "completed checklist" active-use metrics; pure native moat.
- *Trader:* the app becomes a coach that pokes you at the right moments instead of a passive
  ledger you forget.

**Scope sketch (v1).**
- Post-market reminder ("Log today's trades / run your post-market checklist") at a
  user-set time, default ~16:00 IST, weekdays only.
- Pre-market reminder ("Run your pre-market checklist") default ~09:00 IST, opt-in.
- Weekly review reminder (ties to Pillar 4), default Friday evening or Saturday morning.
- Optional rule-break alert: if the trader logs trades during the day and crosses a limit set
  in Pillar 3, fire a "You've hit your daily loss limit" notification.
- Settings screen: per-notification enable + time; respect `POST_NOTIFICATIONS` permission
  (already declared in the manifest) with a proper rationale prompt on Android 13+.
- Quiet by default: nothing fires until the user opts in during onboarding or from Settings.

**Native-Android angle.** `WorkManager` periodic work or `AlarmManager` for exact-time
prompts; notification channels per reminder type so users can tune them in system settings.
A future **home-screen widget** (today's P&L / streak / "checklist 4/13") is a natural
extension — noted, not in v1.

**Dependencies.** Light coupling to Pillar 1 (deep-link into the checklist screen) and Pillar 3
(limit data for the rule-break alert). Can ship the two core reminders before either.

**Risks.** Notification fatigue → keep the default set minimal and easy to silence. Exact-alarm
restrictions on newer Android → prefer `WorkManager` windows unless exact timing is essential.

**Rough effort.** Small–Medium.

---

### Pillar 3 — Trading plan / risk rules

**Problem.** Traders blow up by breaking their own rules — oversizing, overtrading, revenge
trading past a daily loss. Nothing in the app currently captures what the trader's rules *are*,
so it cannot tell them when they broke one.

**Value.**
- *PM:* creates structured, per-user config that powers the discipline score, the rule-break
  nudge (Pillar 2), and the weekly review (Pillar 4) — one feature, three consumers. Strong
  "prop-firm rules" mental model that resonates with the target trader.
- *Trader:* a written plan plus automatic "you broke rule X on 3 days this week" feedback is
  exactly the accountability a solo trader has no one else to provide.

**Scope sketch (v1).**
- A **Trading Plan** the user sets once (editable): max loss per day, max trades per day, max
  risk per trade (₹ or % of capital), max position size, allowed instruments/segments
  (optional), trading window (optional).
- On each logged trade, evaluate it against the plan; store which rules it violated.
- Surface violations: a badge on the trade, "rule-break days" marked on the Analytics calendar,
  and a rule-adherence figure fed into the discipline score (replaces / augments the current
  stop-loss-only component).
- No hard blocking — TradeTrack logs after the fact; this is feedback, not enforcement.

**Native-Android angle.** Minimal — value is in the model and analytics. Pairs with Pillar 2
for real-time alerts if the user logs intraday.

**Dependencies.** Reuses `Trade` data and the mistake-tag vocabulary. Feeds Pillars 1, 2, 4.
Should be specced after Pillar 1 ships so the discipline-score integration is concrete.

**Risks.** Scope creep toward a full position-sizing calculator — keep v1 to "set limits,
detect violations." Defining "risk per trade" needs planned-stop data on the trade, which
TradeTrack may not capture yet (see §5).

**Rough effort.** Medium.

---

### Pillar 4 — Guided weekly review

**Problem.** Data without reflection does not change behaviour. Traders rarely sit down and
review; when they do, it is unstructured. The app has all the raw material and does nothing to
turn it into a ritual.

**Value.**
- *PM:* a weekly high-intent session in the app; produces retained content (review entries);
  natural payload for the weekly notification; reinforces the whole loop.
- *Trader:* 10 minutes a week that converts a pile of trades into three lessons and one
  commitment — the habit that actually compounds.

**Scope sketch (v1).**
- A **Review** flow (button on Dashboard + weekly notification) that walks the trader through a
  fixed set of prompts for the past 7 days:
  - auto-surfaced facts: P&L, win rate, best / worst trade, number of rule-breaks (Pillar 3),
    checklist completion (Pillar 1), most frequent mistake tag;
  - free-text prompts: top 3 things that went well, top 3 mistakes, one lesson, one commitment
    for next week.
- Saved as a dated **review entry**; list of past reviews viewable; last week's commitment shown
  at the top of this week's review ("Did you keep it?").
- Monthly variant later; v1 is weekly only.

**Native-Android angle.** Low. Benefits from offline (compose the review on a commute) and from
Pillar 2's scheduled prompt.

**Dependencies.** Consumes Pillars 1 and 3 for the auto-surfaced facts, but can ship with just
trade/mistake data if those aren't ready. Storage can reuse the checklist sync pattern.

**Risks.** Feeling like a form. Keep prompts few, pre-fill everything computable, make it
skippable section by section.

**Rough effort.** Small–Medium.

---

### Pillar 5 — CSV / tradebook import

**Problem.** A new user's journal is empty, so the analytics and the discipline loop have
nothing to work with for weeks — the classic time-to-value gap. tradediary.in markets "broker
sync" hard but, per the teardown, it appears shallow in practice and needs broker partnerships /
OAuth.

**Value.**
- *PM:* removes the biggest onboarding drop-off; delivers ~80% of "broker sync" perceived value
  with none of the partnership/OAuth cost; a concrete answer to tradediary.in's loudest claim.
- *Trader:* "import my last 6 months from Zerodha" in one step instead of typing 200 trades.

**Scope sketch (v1).**
- Import a broker P&L / tradebook export (CSV / XLSX) via the Android file picker.
- A **column-mapping step**: detect common formats for the major Indian brokers (Zerodha
  Console, Upstox, Groww, Dhan, Angel One) with saved presets; let the user map columns
  manually for anything unrecognised.
- Preview + dedupe (match on symbol + date + qty + price against existing trades) before
  committing.
- Map into the existing `Trade` model; imported trades flagged as `source = IMPORT`.
- Round-trip: also offer **export** of the user's own data as CSV/JSON (cheap trust feature,
  contrasts with tradediary.in's no-refund lock-in vibe).

**Native-Android angle.** Storage Access Framework file picker; parsing on-device
(`Dispatchers.Default`); works offline, syncs imported rows through the existing trade sync.

**Dependencies.** None blocking — touches only the trade layer. Independent of Pillars 1–4;
could be built in parallel by a second workstream.

**Risks.** Broker export formats drift and vary by segment (equity vs F&O vs currency). Keep
the mapping engine generic and preset-driven so a format change is a config update, not a code
change. Charges/brokerage columns are inconsistent — import gross, let the user's existing fee
handling apply.

**Rough effort.** Medium.

---

## 4. Sequencing

Recommended order, assuming a single primary workstream:

1. **Pillar 1 — Discipline checklist.** Already specced. Establishes the discipline-score
   model, the sync-copy pattern for a new Room+Firestore entity, and the first real test suite.
2. **Pillar 5 — CSV import** *(can run in parallel)*. Independent; biggest immediate
   activation win; unblocks everything else by giving new users data.
3. **Pillar 2 — Notifications & nudges (core two reminders).** Small, high retention ROI, and
   makes Pillars 1 and 4 actually get used.
4. **Pillar 3 — Trading plan / risk rules.** Spec after Pillar 1 ships so the score integration
   is concrete.
5. **Pillar 4 — Guided weekly review.** Last, because it is most valuable once 1–3 are feeding
   it real facts. Add the weekly reminder to Pillar 2 at this point.

Each pillar: `brainstorming → spec in docs/superpowers/specs/ → writing-plans → implement`.

## 5. Cross-cutting open questions

- **Planned-stop data on trades.** Pillars 1 and 3 both want "planned vs actual risk". Confirm
  whether `Trade` captures a planned stop / target today; if not, adding those optional fields
  is a shared prerequisite worth doing early.
- **Mistake-tag vocabulary.** The discipline score and rule-break detection both key off
  `Trade.mistakes` strings. Nail down the canonical set (and whether it is RemoteConfig-driven)
  once, and reuse.
- **Partial fills / scaling in-out.** Not a pillar, but flagged in brainstorming: if a "trade"
  is one entry + one exit today, active intraday traders and CSV import (Pillar 5) will both
  strain that model. Assess whether an executions→position model is a prerequisite for Pillar 5.
- **Notification permission UX.** Pillar 2 needs a considered Android 13+ permission rationale;
  decide whether to ask during onboarding or lazily on first opt-in.

## 6. Explicitly deferred (Tier 2 / Tier 3)

Not in this roadmap; revisit after the loop is in place:

- Tier 2: playbook / setups upgrade (entry criteria + A+ grading), annual P&L / tax report
  export (India-specific), shareable performance card.
- Tier 3: home-screen widget, mood/energy check-in with win-rate correlation, goals & streaks,
  expectancy / Monte-Carlo simulator, multi-execution position model.
- From the competitor teardown, still deliberately **not** doing: PaperDesk-style options
  simulator, a social Community.
