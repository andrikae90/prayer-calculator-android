import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APP_LANGUAGE = ROOT / "app/src/main/java/com/example/ui/localization/AppLanguage.kt"
UI_ROOT = ROOT / "app/src/main/java/com/example/ui"

# AppLanguage keeps Indonesian text as the stable translation key.
source = APP_LANGUAGE.read_text(encoding="utf-8")
keys = set(re.findall(r'^\s*"((?:[^"\\]|\\.)+)"\s+to\s+mapOf', source, re.MULTILINE))
keys = {bytes(k, "utf-8").decode("unicode_escape") for k in keys}

PREFIX = "com.example.ui.localization.AppLanguage.text(com.example.ui.localization.LocalAppLanguage.current, "

def replacement(match):
    value = match.group(1)
    if value not in keys:
        return match.group(0)
    return f'text = {PREFIX}"{value}")'

for path in UI_ROOT.rglob("*.kt"):
    if path == APP_LANGUAGE:
        continue
    text = path.read_text(encoding="utf-8")

    # Localize literal Text(text = "...") calls whose keys already exist.
    text = re.sub(r'text = "((?:[^"\\]|\\.)+)"\)', replacement, text)

    # Localize common accessibility labels that are direct translation keys.
    def cd_repl(match):
        value = match.group(1)
        if value not in keys:
            return match.group(0)
        return f'contentDescription = {PREFIX}"{value}")'

    text = re.sub(r'contentDescription = "((?:[^"\\]|\\.)+)"\)', cd_repl, text)

    # Home's prayer-name fallback is data-driven but its displayName is an
    # Indonesian translation key, so pass it through the same localization map.
    text = text.replace(
        'nextPrayer?.type?.displayName ?: "Subuh"',
        f'{PREFIX}(nextPrayer?.type?.displayName ?: "Subuh"))'
    )

    path.write_text(text, encoding="utf-8")
