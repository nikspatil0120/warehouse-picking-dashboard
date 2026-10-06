# Branching Strategy

The Warehouse Picking Dashboard uses a simplified Git Flow branching strategy.

## Main Branch

`main`

- Contains stable and release-ready code.
- Direct commits should not be made to `main`.
- Changes should be merged through pull requests after review.

## Develop Branch

`develop`

- Acts as the integration branch.
- Completed features are merged into `develop` before release.
- Direct commits should be avoided.

## Feature Branches

Format:

```text
feature/<name>