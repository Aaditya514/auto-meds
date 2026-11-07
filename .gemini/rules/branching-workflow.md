# Branching Workflow Rule

Whenever making updates to this codebase:
1. **Branch first**: Always create a new feature branch from `main` (e.g. `feature/<feature-name>` or `fix/<fix-name>`). Never work directly on `main`.
2. **Implement**: Develop the changes on that feature branch.
3. **Verify & Test**: Check and verify that the branch changes are working fine (run backend tests, compile frontend `ng build`, ensure zero errors).
4. **Merge & Push**: Merge the verified branch into `main` and push to `origin main`.
