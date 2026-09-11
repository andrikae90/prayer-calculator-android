import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APP_LANGUAGE = ROOT / "app/src/main/java/com/example/ui/localization/AppLanguage.kt"
UI_ROOT = ROOT / "app/src/main/java/com/example/ui"

# Keep the Indonesian UI spelling consistent: use "sholat" instead of "salat".
source = APP_LANGUAGE.read_text(encoding="utf-8")
source = source.replace("Salat", "Sholat").replace("salat", "sholat")
APP_LANGUAGE.write_text(source, encoding="utf-8")

# AppLanguage keeps Indonesian text as the stable translation key.
keys = set(re.findall(r'^\s*"((?:[^"\\]|\\.)+)"\s+to\s+mapOf', source, re.MULTILINE))
keys = {bytes(k, "utf-8").decode("unicode_escape") for k in keys}

PREFIX = "com.example.ui.localization.AppLanguage.text(com.example.ui.localization.LocalAppLanguage.current, "


def localize_text(match):
    value = match.group(1)
    if value not in keys:
        return match.group(0)
    tail = match.group(2)
    return f'text = {PREFIX}"{value}"){tail}'


def localize_content_description(match):
    value = match.group(1)
    if value not in keys:
        return match.group(0)
    tail = match.group(2)
    return f'contentDescription = {PREFIX}"{value}"){tail}'

for path in UI_ROOT.rglob("*.kt"):
    if path == APP_LANGUAGE:
        continue
    text = path.read_text(encoding="utf-8")

    # Handles both `Text(text = "...", ...)` and `Text(text = "...")`.
    text = re.sub(
        r'text = "((?:[^"\\]|\\.)+)"([,)])',
        localize_text,
        text,
    )

    # Localize common accessibility labels that are direct translation keys.
    text = re.sub(
        r'contentDescription = "((?:[^"\\]|\\.)+)"([,)])',
        localize_content_description,
        text,
    )

    # Home's prayer-name fallback is data-driven but its displayName is an
    # Indonesian translation key, so pass it through the same localization map.
    text = text.replace(
        'nextPrayer?.type?.displayName ?: "Subuh"',
        f'{PREFIX}(nextPrayer?.type?.displayName ?: "Subuh"))'
    )

    path.write_text(text, encoding="utf-8")
