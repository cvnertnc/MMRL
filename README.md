# Magisk Modules Repo Loader (MMRL)

The Magisk Module Repo Loader (MMRL). Allows you to load custom repos with an simple UI

## How to create custom repo?

At first need you to fork [Magisk-Modules-Alt-Repo/json](https://github.com/Magisk-Modules-Alt-Repo/json) or create yourself one

### Configuration

Change `Magisk-Modules-Alt-Repo` to your username or something where is hosted.

```py
# Configuration
REPO_NAME = "Magisk-Modules-Alt-Repo"
REPO_TITLE = "Magisk Modules Alt Repo"
```

At least can you edit the json output

> Please leave all default created objects!

```py
# Create meta module information
module = {
    "id": moduleprop["id"],
    "last_update": int(repo.updated_at.timestamp() * 1000),
    "prop_url": f"https://raw.githubusercontent.com/{repo.full_name}/{repo.default_branch}/module.prop",
    "zip_url": f"https://github.com/{repo.full_name}/archive/{repo.default_branch}.zip",
    "notes_url": f"https://raw.githubusercontent.com/{repo.full_name}/{repo.default_branch}/README.md",
    "stars": int(repo.stargazers_count)
}
```

## FAQ

### Why does some description reports `404: Not Found`?

Always create an `README.md` not `readme.md` or something.

### Module props like `changeBoot` or something are not displayed?

The module doesn't have this prop.

### What are low-quality modules?

These modules doens't have props like `version`, `versionCode`, `description`, `id` or `author`

### Why does my module aren't displayed?

The MMRL has an own hidding system. This means that bad modules can hidden from an admin.

### How to get an verified module?

This depends on the general module quality. (See `What are low-quality modules?`). Not all modules get an verified badge.
