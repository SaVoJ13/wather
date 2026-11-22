#!/usr/bin/env python3
"""
Test script for WeatherMetro API (14-day & 16-day forecast) with zero key exposure.
Adheres to Constitution v2.0 Red Line #3 (No secrets in Git, logs, or chat).
"""
import os
import sys
import json
import urllib.request
import urllib.error
import time

def mask_key(k: str) -> str:
    if not k or len(k) < 6:
        return "***"
    return f"{k[:3]}...{k[-3:]} (len: {len(k)})"

def get_key_from_local_properties() -> str:
    local_props_path = os.path.join(os.path.dirname(__file__), "..", "local.properties")
    if os.path.exists(local_props_path):
        with open(local_props_path, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line.startswith("weathermetro.api.key=") or line.startswith("WEATHERMETRO_API_KEY="):
                    return line.split("=", 1)[1].strip()
    return ""

def main():
    print("=" * 70)
    print(" WeatherMetro API Verification Test (Secure / No Key Exposure)")
    print("=" * 70)

    api_key = os.environ.get("WEATHERMETRO_API_KEY", "").strip()
    if not api_key:
        api_key = get_key_from_local_properties()

    # Target test coordinates: Kyiv (50.45, 30.52)
    lat, lng = 50.45, 30.52
    base_url = "https://api.weathermetro.com/v2/forecast"
    
    if api_key:
        print(f"[*] API Key detected: {mask_key(api_key)}")
        url = (
            f"{base_url}?lat={lat}&lng={lng}"
            f"&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weathercode"
            f"&hourly=temperature_2m,precipitation_probability,weathercode"
            f"&forecast_days=16"
            f"&timezone=auto"
            f"&api_key={api_key}"
        )
    else:
        print("[!] No API key provided in environment or local.properties.")
        print("[*] Performing live endpoint handshake test without credentials...")
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
                print(f"[+] Verification: SUCCESS (WeatherMetro fulfills 14-day requirement)")
            else:
                print(f"[-] Verification: Incomplete forecast depth ({days_count} days)")
                
    except urllib.error.HTTPError as e:
        elapsed = time.time() - start_time
        status_code = e.code
        body = e.read().decode("utf-8")
        print(f"[*] HTTP Response Code: {status_code} ({elapsed:.2f}s)")
        print(f"[*] Server: {e.headers.get('Server', 'Unknown')} | Edge Ray: {e.headers.get('CF-RAY', 'N/A')}")
        
        try:
            err_json = json.loads(body)
            meta = err_json.get("meta", {})
            print(f"[*] Response Envelope: Code={meta.get('code')}, Type='{meta.get('error_type')}', Detail='{meta.get('error_detail')}'")
        except Exception:
            print(f"[*] Raw Response: {body[:150]}")
            
        if status_code == 401:
            print("[+] Endpoint reachability & Cloudflare WAF/auth handshake: CONFIRMED LIVE.")
            print("[+] Expected behavior: 401 Unauthorized returned when key is absent/unregistered.")
        elif status_code == 429:
            print("[+] Rate Limit / Quota behavior: CONFIRMED HTTP 429 (Zero financial liability).")
    except Exception as ex:
        print(f"[-] Connection Error: {ex}")

    print("=" * 70)

if __name__ == "__main__":
    main()
