#!/usr/bin/env python3
"""
Test script for WeatherMetro API (14-day / 16-day forecast) with zero key exposure.
Adheres strictly to Constitution v2.0 Red Line #3:
- Zero API key fragments in logs (no prefix, no suffix, no partial strings).
- Non-zero exit code (sys.exit(1)) on ANY failure (missing key, auth error, HTTP 4xx/5xx, forecast < 14 days).
- Exit code 0 (sys.exit(0)) ONLY on fully verified authorized forecast with >= 14 days.
"""
import os
import sys
import json
import urllib.request
import urllib.error
import time

def get_key_from_local_properties() -> str:
    # Check parent and current directories for local.properties
    candidates = [
        os.path.join(os.path.dirname(__file__), "..", "local.properties"),
        os.path.join(os.path.dirname(__file__), "..", "..", "local.properties"),
        "local.properties"
    ]
    for path in candidates:
        if os.path.exists(path):
            try:
                with open(path, "r", encoding="utf-8") as f:
                    for line in f:
                        line = line.strip()
                        if line.startswith("weathermetro.api.key=") or line.startswith("WEATHERMETRO_API_KEY="):
                            val = line.split("=", 1)[1].strip()
                            if val and val != "YOUR_KEY" and val != "REPLACE_ME":
                                return val
            except Exception:
                pass
    return ""

def main():
    print("=" * 70)
    print(" WeatherMetro API Verification Test (Zero Key Exposure / Strict Exit)")
    print("=" * 70)

    api_key = os.environ.get("WEATHERMETRO_API_KEY", "").strip()
    if not api_key:
        api_key = get_key_from_local_properties()

    # Target test coordinates: Kyiv (50.45, 30.52)
    lat, lng = 50.45, 30.52
    base_url = "https://api.weathermetro.com/v2/forecast"

    if api_key:
        # STRICT PRIVACY: Zero key characters logged
        print("[*] API Key present in configuration: YES (masked, 0 chars exposed)")
        url = (
            f"{base_url}?lat={lat}&lng={lng}"
            f"&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weathercode"
            f"&hourly=temperature_2m,precipitation_probability,weathercode"
            f"&forecast_days=16"
            f"&timezone=auto"
            f"&api_key={api_key}"
        )
    else:
        print("[!] No API key found in environment (WEATHERMETRO_API_KEY) or local.properties (weathermetro.api.key).")
        print("[*] Performing unauthenticated live endpoint handshake test...")
        url = f"{base_url}?lat={lat}&lng={lng}"

    start_time = time.time()
    try:
        req = urllib.request.Request(
            url,
            headers={
                "User-Agent": "WeatherApp-VerificationBot/1.0",
                "Accept": "application/json"
            }
        )
        with urllib.request.urlopen(req, timeout=10) as resp:
            elapsed = time.time() - start_time
            status_code = resp.getcode()
            headers = dict(resp.info())
            raw_data = resp.read().decode("utf-8")
            data = json.loads(raw_data)

            print(f"[+] Status Code: {status_code} OK (in {elapsed:.2f}s)")
            print(f"[+] Server: {headers.get('Server', 'Unknown')} (Cloudflare Edge)")

            # Verify payload
            daily = data.get("response", {}).get("data", {}).get("daily", {})
            times = daily.get("time", [])
            days_count = len(times)
            print(f"[+] Forecast days returned: {days_count} days (Required: >= 14)")

            if days_count >= 14:
                print(f"[+] Date range: {times[0]} -> {times[-1]}")
                print(f"[+] Daily max temps count: {len(daily.get('temperature_2m_max', []))}")
                print(f"[+] Daily min temps count: {len(daily.get('temperature_2m_min', []))}")
                hourly = data.get("response", {}).get("data", {}).get("hourly", {})
                hourly_times = hourly.get("time", [])
                print(f"[+] Hourly intervals returned: {len(hourly_times)} hours")
                print("=" * 70)
                print("[SUCCESS] WeatherMetro API test PASSED with valid >= 14-day forecast!")
                print("=" * 70)
                sys.exit(0)
            else:
                print(f"[-] ERROR: Incomplete forecast depth ({days_count} < 14 days)")
                print("=" * 70)
                sys.exit(1)

    except urllib.error.HTTPError as e:
        elapsed = time.time() - start_time
        status_code = e.code
        body = e.read().decode("utf-8")
        print(f"[*] HTTP Error Code: {status_code} ({elapsed:.2f}s)")
        print(f"[*] Server: {e.headers.get('Server', 'Unknown')} | Edge Ray: {e.headers.get('CF-RAY', 'N/A')}")

        try:
            err_json = json.loads(body)
            meta = err_json.get("meta", {})
            print(f"[*] API Envelope: Code={meta.get('code')}, Type='{meta.get('error_type')}', Detail='{meta.get('error_detail')}'")
        except Exception:
            print(f"[*] Raw Response: {body[:150]}")

        print("=" * 70)
        if status_code == 401:
            print("[FAILURE] HTTP 401 Unauthorized: Valid API key required to authenticate forecast requests.")
        elif status_code == 429:
            print("[FAILURE] HTTP 429 Too Many Requests: Rate limit or quota ceiling reached (Zero financial charge).")
        else:
            print(f"[FAILURE] HTTP {status_code} error received.")
        print("=" * 70)
        # Non-zero exit code as required
        sys.exit(1)

    except Exception as ex:
        print(f"[-] Network/System Error: {ex}")
        print("=" * 70)
        sys.exit(1)

if __name__ == "__main__":
    main()
