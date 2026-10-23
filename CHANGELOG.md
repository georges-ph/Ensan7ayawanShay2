# Changelog

All notable changes to this project are documented here.
Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [2.1.0] - 2026-10-23

### Added
- "Forgot password?" link on the sign-in screen: sends a reset link to the
  email typed in the form
- Loading screen on the web: the app icon fills up while the game loads,
  instead of a blank page
- Project landing page on GitHub Pages

### Changed
- Security rules are no longer deployed from this repository; reference
  copies are in `docs/` for anyone running their own Firebase project

## [2.0.0] - 2026-09-26

Full Flutter rewrite of the original native Android app, targeting Android and Web.
Live multiplayer, presence, and light/dark theme carry over from the original app.

### Added
- Email/password sign-in, alongside the existing Google sign-in
- Invite-code rooms, replacing the old flow of picking a player from a list
- Round counter and synced round timer

### Changed
- Redesigned UI with a new color palette, typography, and components

## [1.1] - 2022-12-28

Last release of the original Java/Android version. See `git log` history prior to the
Flutter rewrite for details.

[2.1.0]: https://github.com/georges-ph/Ensan7ayawanShay2/releases/tag/v2.1.0
[2.0.0]: https://github.com/georges-ph/Ensan7ayawanShay2/releases/tag/v2.0.0
[1.1]: https://github.com/georges-ph/Ensan7ayawanShay2/releases/tag/v1.1
