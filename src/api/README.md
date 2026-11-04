# gms-api

> Gym Management System - Backend API: business logic and REST endpoints

Part of the **Gym Management System** full-stack project — team `gym-management-system`, Complementaria III - Desarrollo Fullstack 2026-B.
Governance and documentation live in [`gms-docs`](https://github.com/code-corhuila/gms-docs).

## Branching

Three permanent branches. **None of them accepts a direct commit** — you enter through a child
branch and leave through a Pull Request.

```
develop  <--PR--  feat/... fix/... chore/...
qa       <--PR--  develop
main     <--PR--  qa (release)   hotfix/...
```

`develop` is the default branch: start every task from it. `main` requires **1 approval from
`ariel5253`**. On `develop` and `qa` the team sets its own review rule.

Commit and branch naming: `00-governance/git-conventions.md` in `gms-docs`.
