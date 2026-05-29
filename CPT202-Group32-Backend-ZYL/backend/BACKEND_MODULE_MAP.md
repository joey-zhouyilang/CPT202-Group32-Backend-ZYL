# Backend Module Map

This backend is organized by owner-prefixed modules under `src/main/java/com/group32/cpt202`.

## Module ownership

| Prefix | Folder | Primary responsibility | Main API prefix |
| --- | --- | --- | --- |
| `ZYL` | `zyl_project` | login, registration, homepage summary, profile update | `/api/zyl`, `/api/zyl/display` |
| `LY` | `LY_contributor` | contributor application and admin review | `/api/ly-contributor/...` |
| `LY` | `LY_heritage` | heritage detail page and comment interaction | `/api/ly-heritage/heritages` |
| `CY` | `CY_project` | workflow review for contributor audit and heritage item audit | `/api/cy/workflow` |
| `LPP` | `lpp_project` | contributor resource draft, submit, publish, reject, archive | `/api/lpp/resources`, `/api/lpp/admin/resources` |

## Frontend page to backend module mapping

| Frontend function | Backend module |
| --- | --- |
| Login / Register | `zyl_project/zyl_login` |
| Home basic information | `zyl_project/zyl_display` |
| Profile update | `zyl_project/zyl_login` |
| Ordinary user application page | `LY_contributor` |
| Admin contributor review | `LY_contributor` |
| Heritage detail page | `LY_heritage` |
| Interaction and comments | `LY_heritage` |
| Contributor resource draft and submit | `lpp_project` |
| Admin resource publish / reject / archive | `lpp_project` |
| Workflow audit and resubmission | `CY_project` |

## Integration rules

1. Shared user data uses `LY_contributor.entity.User` and `LY_contributor.repository.UserRepository`.
2. Shared heritage data uses `LY_heritage.entity.HeritageItem` and `LY_heritage.repository.HeritageItemRepository`.
3. `zyl_project` uses adapter-style classes and does not define duplicate JPA entities.
4. `CY_project` stays focused on workflow and audit APIs, with MyBatis under `CY_project.mapper`.
5. `lpp_project` writes contributor resources into the shared `heritage_item` table, so published content can be displayed by `LY_heritage` and `zyl_project`.
