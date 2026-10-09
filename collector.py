#!/usr/bin/env python3
"""
AegisGuard - Gerçek Veri Toplayıcı ve Akış Entegratörü (Tamamen Ücretsiz)
Bu script, harici hiçbir API ücreti ödemeden gerçek veya özel sosyal medya gönderilerini
yerel Spring Boot AegisGuard sunucunuza (http://localhost:8080/api/post) pompalar.
"""

import sys
import json
import urllib.request
import urllib.error
import time
from datetime import datetime

BACKEND_URL = "http://localhost:8080/api/post"

def send_to_aegis(post_data):
    """Gönderiyi yerel Java sunucusuna HTTP POST ile gönderir."""
    try:
        req = urllib.request.Request(
            BACKEND_URL,
            data=json.dumps(post_data).encode("utf-8"),
            headers={"Content-Type": "application/json"}
        )
        with urllib.request.urlopen(req, timeout=5) as response:
            if response.status == 200:
                result = json.loads(response.read().decode("utf-8"))
                score = round(result.get("manipulationScore", 0))
                threat = result.get("threatLevel", "UNKNOWN")
                print(f"[{datetime.now().strftime('%H:%M:%S')}] Gönderildi -> @{post_data['authorUsername']}: Tehdit: {threat} (%{score})")
                if result.get("flagReasons"):
                    for r in result["flagReasons"]:
                        print(f"   ⚠️ {r}")
                return result
    except Exception as e:
        print(f"Hata: Java sunucusuna bağlanılamadı ({BACKEND_URL}). Sunucu açık mı? Detay: {e}")
        return None

def interactive_mode():
    """Kullanıcının terminalden gerçek bir tweet/yorum yapıştırıp anında analiz etmesini sağlar."""
    print("=" * 60)
    print("🛡️  AegisGuard - Gerçek Gönderi Canlı Giriş Terminali")
    print("=" * 60)
    print("Twitter/Instagram'da gördüğün herhangi bir şüpheli tweet'i yapıştırabilirsin.")
    print("Çıkmak için 'q' yazıp Enter'a basabilirsin.\n")

    while True:
        try:
            text = input("\n📝 Gönderi Metni: ").strip()
            if not text or text.lower() == 'q':
                break

            username = input("👤 Kullanıcı Adı (Varsayılan: anon_user): ").strip() or "anon_user"
            followers_input = input("👥 Takipçi Sayısı (Varsayılan: 50): ").strip()
            followers = int(followers_input) if followers_input.isdigit() else 50

            following_input = input("🚶 Takip Edilen Sayısı (Varsayılan: 200): ").strip()
            following = int(following_input) if following_input.isdigit() else 200

            age_input = input("📅 Hesap Yaşı - Gün (Varsayılan: 30): ").strip()
            age = int(age_input) if age_input.isdigit() else 30

            avatar_input = input("🖼️ Varsayılan Avatar mı? (e/h - Varsayılan: h): ").strip().lower()
            default_avatar = (avatar_input == 'e')

            post = {
                "content": text,
                "authorUsername": username,
                "authorFollowers": followers,
                "authorFollowing": following,
                "authorAccountAgeDays": age,
                "hasDefaultAvatar": default_avatar,
                "platform": "Manuel Giriş / Terminal"
            }

            print("\nAnaliz ediliyor...")
            send_to_aegis(post)
            print("-" * 50)

        except KeyboardInterrupt:
            break

if __name__ == "__main__":
    interactive_mode()
