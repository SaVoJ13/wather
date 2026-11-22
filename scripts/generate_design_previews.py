"""
Complete Python script to render all 8 high-resolution PNG mockups
for Weather App Phase 1 Final Design Gate into docs/design/previews/.
"""
import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUTPUT_DIR = r"C:\Users\cocro\Wather\docs\design\previews"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# System fonts
FONT_REGULAR_PATH = r"C:\Windows\Fonts\segoeui.ttf"
FONT_BOLD_PATH = r"C:\Windows\Fonts\segoeuib.ttf"
FONT_SEMIBOLD_PATH = r"C:\Windows\Fonts\segoeuisl.ttf"

def get_font(size, bold=False, semibold=False):
    path = FONT_BOLD_PATH if bold else (FONT_SEMIBOLD_PATH if semibold else FONT_REGULAR_PATH)
    try:
        return ImageFont.truetype(path, size)
    except Exception:
        return ImageFont.load_default()

W, H = 824, 1830  # Standard mobile resolution at 2x

def create_gradient(w, h, top_rgb, bottom_rgb):
    base = Image.new("RGBA", (w, h))
    draw = ImageDraw.Draw(base)
    for y in range(h):
        r = int(top_rgb[0] + (bottom_rgb[0] - top_rgb[0]) * (y / h))
        g = int(top_rgb[1] + (bottom_rgb[1] - top_rgb[1]) * (y / h))
        b = int(top_rgb[2] + (bottom_rgb[2] - top_rgb[2]) * (y / h))
        draw.line([(0, y), (w, y)], fill=(r, g, b, 255))
    return base

def draw_card(img, xy, radius, fill_rgba, outline_rgba=None, outline_width=1):
    card = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(card)
    d.rounded_rectangle(xy, radius=radius, fill=fill_rgba, outline=outline_rgba, width=outline_width)
    img.alpha_composite(card)

def draw_status_bar(draw, dark_icons=False):
    fill = (20, 20, 20, 255) if dark_icons else (245, 245, 245, 255)
    f = get_font(26, bold=True)
    draw.text((44, 22), "14:30", fill=fill, font=f)
    # Icons: wifi, cellular, battery on right
    draw.rectangle([700, 26, 742, 46], outline=fill, width=2)
    draw.rectangle([703, 29, 734, 43], fill=fill)
    draw.rectangle([743, 31, 746, 41], fill=fill)
    # Wifi arc
    draw.arc([655, 22, 685, 52], start=200, end=340, fill=fill, width=3)
    draw.ellipse([668, 42, 672, 46], fill=fill)

def draw_nav_bar(img, active_tab_index=0, dark_theme=False):
    nav_h = 160
    nav_y = H - nav_h
    bg_rgba = (15, 23, 42, 245) if dark_theme else (255, 255, 255, 248)
    border_rgba = (255, 255, 255, 30) if dark_theme else (0, 0, 0, 20)
    draw_card(img, (0, nav_y, W, H), radius=0, fill_rgba=bg_rgba, outline_rgba=border_rgba)
    
    draw = ImageDraw.Draw(img)
    tabs = [("☀️", "Зараз"), ("📅", "Прогноз"), ("📍", "Місця")]
    tab_w = W // 3
    
    for i, (icon, label) in enumerate(tabs):
        cx = i * tab_w + tab_w // 2
        is_active = (i == active_tab_index)
        
        if is_active:
            pill_color = (59, 130, 246, 50) if dark_theme else (219, 234, 254, 255)
            pill_border = (96, 165, 250, 180) if dark_theme else (59, 130, 246, 200)
            draw.rounded_rectangle([cx - 72, nav_y + 16, cx + 72, nav_y + 68], radius=26, fill=pill_color, outline=pill_border, width=2)
            icon_fill = (96, 165, 250, 255) if dark_theme else (29, 78, 216, 255)
            text_fill = (255, 255, 255, 255) if dark_theme else (15, 23, 42, 255)
            f_label = get_font(22, bold=True)
        else:
            icon_fill = (148, 163, 184, 255) if dark_theme else (100, 116, 139, 255)
            text_fill = (148, 163, 184, 255) if dark_theme else (100, 116, 139, 255)
            f_label = get_font(20, semibold=True)
            
        f_icon = get_font(30)
        draw.text((cx - 15, nav_y + 24), icon, fill=icon_fill, font=f_icon)
        tb = draw.textbbox((0, 0), label, font=f_label)
        tw = tb[2] - tb[0]
        draw.text((cx - tw // 2, nav_y + 80), label, fill=text_fill, font=f_label)
        
    handle_fill = (255, 255, 255, 120) if dark_theme else (15, 23, 42, 100)
    draw.rounded_rectangle([W // 2 - 80, H - 20, W // 2 + 80, H - 12], radius=4, fill=handle_fill)

def draw_admob_banner(img, dark_theme=False, show_skeleton=False):
    banner_h = 100
    banner_y = H - 160 - banner_h - 12
    banner_w = W - 48
    
    box_rgba = (30, 41, 59, 240) if dark_theme else (248, 250, 252, 250)
    border_rgba = (71, 85, 105, 150) if dark_theme else (203, 213, 225, 200)
    draw_card(img, (24, banner_y, 24 + banner_w, banner_y + banner_h), radius=16, fill_rgba=box_rgba, outline_rgba=border_rgba, outline_width=2)
    
    draw = ImageDraw.Draw(img)
    if show_skeleton:
        skel_fill = (51, 65, 85, 180) if dark_theme else (226, 232, 240, 200)
        draw.rounded_rectangle([44, banner_y + 20, 180, banner_y + 48], radius=8, fill=skel_fill)
        draw.rounded_rectangle([44, banner_y + 58, 480, banner_y + 82], radius=6, fill=skel_fill)
        draw.rounded_rectangle([W - 190, banner_y + 24, W - 44, banner_y + 76], radius=12, fill=skel_fill)
        f_badge = get_font(18, bold=True)
        draw.text((W - 135, banner_y + 38), "AD", fill=(148, 163, 184, 255), font=f_badge)
    else:
        # Green Test Ad badge
        draw.rounded_rectangle([40, banner_y + 14, 94, banner_y + 38], radius=6, fill=(22, 163, 74, 240))
        f_badge = get_font(16, bold=True)
        draw.text((49, banner_y + 17), "Тест", fill=(255, 255, 255, 255), font=f_badge)
        
        f_title = get_font(22, bold=True)
        f_sub = get_font(16)
        title_fill = (241, 245, 249, 255) if dark_theme else (15, 23, 42, 255)
        sub_fill = (148, 163, 184, 255) if dark_theme else (100, 116, 139, 255)
        draw.text((106, banner_y + 15), "Google AdMob • Тестове оголошення", fill=title_fill, font=f_title)
        draw.text((40, banner_y + 52), "Anchored Adaptive Banner (динамічно 50-90dp) • Zero CLS", fill=sub_fill, font=f_sub)
        
        cta_fill = (37, 99, 235, 255)
        draw.rounded_rectangle([W - 180, banner_y + 24, W - 44, banner_y + 76], radius=14, fill=cta_fill)
        f_cta = get_font(20, bold=True)
        draw.text((W - 162, banner_y + 38), "Встановити", fill=(255, 255, 255, 255), font=f_cta)

def draw_top_bar(img, title, subtitle=None, dark_theme=False, show_back=False):
    draw = ImageDraw.Draw(img)
    top_y = 70
    icon_fill = (255, 255, 255, 255) if dark_theme else (15, 23, 42, 255)
    
    if show_back:
        f_icon = get_font(36, bold=True)
        draw.text((36, top_y + 8), "←", fill=icon_fill, font=f_icon)
        f_title = get_font(32, bold=True)
        draw.text((90, top_y + 10), title, fill=icon_fill, font=f_title)
    else:
        f_pin = get_font(28)
        draw.text((36, top_y + 10), "📍", fill=icon_fill, font=f_pin)
        f_title = get_font(32, bold=True)
        draw.text((76, top_y + 8), title, fill=icon_fill, font=f_title)
        tw = draw.textbbox((0, 0), title, font=f_title)[2]
        draw.text((76 + tw + 10, top_y + 14), "▼", fill=icon_fill, font=get_font(20))
        
        if subtitle:
            f_sub = get_font(18)
            sub_fill = (203, 213, 225, 255) if dark_theme else (100, 116, 139, 255)
            draw.text((76, top_y + 48), subtitle, fill=sub_fill, font=f_sub)
            
        gear_bg = (255, 255, 255, 30) if dark_theme else (0, 0, 0, 15)
        draw.rounded_rectangle([W - 90, top_y + 8, W - 36, top_y + 62], radius=16, fill=gear_bg)
        f_gear = get_font(28)
        draw.text((W - 75, top_y + 18), "⚙️", fill=icon_fill, font=f_gear)

# ==========================================================
# 1. SCREEN 1: NOW LIGHT THEME
# ==========================================================
def render_now_screen_light():
    img = create_gradient(W, H, (74, 144, 226), (234, 244, 254))
    draw = ImageDraw.Draw(img)
    draw_status_bar(draw, dark_icons=False)
    draw_top_bar(img, "Київ, Україна", "Оновлено: 14:30 (WeatherMetro)", dark_theme=False)
    
    # Hero Section
    f_sun = get_font(84)
    draw.text((W // 2 - 46, 170), "☀️", fill=(255, 215, 0, 255), font=f_sun)
    
    f_temp = get_font(120, bold=True)
    draw.text((W // 2 - 145, 270), "+21°", fill=(15, 23, 42, 255), font=f_temp)
    
    f_cond = get_font(34, bold=True)
    tb_c = draw.textbbox((0, 0), "Ясно, без опадів", font=f_cond)
    draw.text((W // 2 - (tb_c[2]-tb_c[0]) // 2, 410), "Ясно, без опадів", fill=(30, 41, 59, 255), font=f_cond)
    
    f_sub = get_font(22, semibold=True)
    sub_txt = "Відчувається як +22°  •  Макс: +24°  Мін: +13°"
    tb_s = draw.textbbox((0, 0), sub_txt, font=f_sub)
    draw.text((W // 2 - (tb_s[2]-tb_s[0]) // 2, 460), sub_txt, fill=(71, 85, 105, 255), font=f_sub)
    
    # 24h Hourly Forecast Row
    card_bg = (255, 255, 255, 190)
    card_border = (255, 255, 255, 220)
    draw_card(img, (24, 520, W - 24, 730), radius=24, fill_rgba=card_bg, outline_rgba=card_border, outline_width=2)
    
    draw.text((48, 540), "ПОГОДИННИЙ ПРОГНОЗ", fill=(100, 116, 139, 255), font=get_font(18, bold=True))
    
    hourly = [
        ("Зараз", "☀️", "+21°", "0%"),
        ("15:00", "☀️", "+22°", "0%"),
        ("16:00", "🌤️", "+21°", "10%"),
        ("17:00", "🌤️", "+20°", "20%"),
        ("18:00", "⛅", "+18°", "15%"),
    ]
    col_w = (W - 96) // 5
    for i, (time_str, icon, temp_str, pop_str) in enumerate(hourly):
        hx = 48 + i * col_w + col_w // 2
        f_h = get_font(20, bold=(i == 0))
        t_fill = (29, 78, 216, 255) if i == 0 else (71, 85, 105, 255)
        tb = draw.textbbox((0, 0), time_str, font=f_h)
        draw.text((hx - (tb[2]-tb[0])//2, 580), time_str, fill=t_fill, font=f_h)
        
        draw.text((hx - 18, 616), icon, fill=(255, 215, 0, 255), font=get_font(32))
        
        f_t = get_font(22, bold=True)
        tb_t = draw.textbbox((0, 0), temp_str, font=f_t)
        draw.text((hx - (tb_t[2]-tb_t[0])//2, 660), temp_str, fill=(15, 23, 42, 255), font=f_t)
        
        f_p = get_font(16)
        tb_p = draw.textbbox((0, 0), pop_str, font=f_p)
        draw.text((hx - (tb_p[2]-tb_p[0])//2, 692), pop_str, fill=(37, 99, 235, 255), font=f_p)

    # 2x2 Weather Metrics Grid
    metrics = [
        ("Вологість", "💧 45%", "Точка роси: 9°", 24, 750, W // 2 - 12, 880),
        ("Вітер", "💨 4.2 м/с", "Пн-Сх, пориви до 7 м/с", W // 2 + 12, 750, W - 24, 880),
        ("Тиск", "🧭 1018 гПа", "764 мм рт.ст. (норма)", 24, 900, W // 2 - 12, 1030),
        ("УФ-індекс", "☀️ 5 помірний", "Захист до 17:00", W // 2 + 12, 900, W - 24, 1030),
    ]
    for title, val, sub, x1, y1, x2, y2 in metrics:
        draw_card(img, (x1, y1, x2, y2), radius=20, fill_rgba=card_bg, outline_rgba=card_border, outline_width=2)
        draw.text((x1 + 24, y1 + 18), title, fill=(100, 116, 139, 255), font=get_font(18, bold=True))
        draw.text((x1 + 24, y1 + 48), val, fill=(15, 23, 42, 255), font=get_font(26, bold=True))
        draw.text((x1 + 24, y1 + 90), sub, fill=(71, 85, 105, 255), font=get_font(18))
        
    # Astronomical Sun Arc Card
    draw_card(img, (24, 1050, W - 24, 1310), radius=24, fill_rgba=card_bg, outline_rgba=card_border, outline_width=2)
    draw.text((48, 1070), "АСТРОНОМІЧНИЙ ДЕНЬ", fill=(100, 116, 139, 255), font=get_font(18, bold=True))
    
    arc_box = [120, 1100, W - 120, 1320]
    draw.arc(arc_box, start=180, end=0, fill=(217, 119, 6, 180), width=6)
    sun_x = int(W/2 + 250 * math.cos(math.radians(240)))
    sun_y = int(1210 + 110 * math.sin(math.radians(240)))
    draw.ellipse([sun_x - 16, sun_y - 16, sun_x + 16, sun_y + 16], fill=(251, 191, 36, 255), outline=(217, 119, 6, 255), width=3)
    
    draw.text((60, 1260), "Схід: 05:42", fill=(15, 23, 42, 255), font=get_font(20, bold=True))
    draw.text((W - 190, 1260), "Захід: 20:18", fill=(15, 23, 42, 255), font=get_font(20, bold=True))
    
    # Attribution
    f_att = get_font(16)
    draw.text((W // 2 - 160, 1420), "Погодні дані від WeatherMetro (weathermetro.com)", fill=(71, 85, 105, 255), font=f_att)
    
    draw_admob_banner(img, dark_theme=False)
    draw_nav_bar(img, active_tab_index=0, dark_theme=False)
    
    path = os.path.join(OUTPUT_DIR, "01_now_screen_light.png")
    img.save(path, "PNG")
    print(f"Saved: {path}")

# ==========================================================
# 2. SCREEN 2: NOW DARK THEME
# ==========================================================
def render_now_screen_dark():
    img = create_gradient(W, H, (10, 17, 40), (30, 41, 59))
    draw = ImageDraw.Draw(img)
    
    stars = [(70, 140), (160, 90), (300, 160), (450, 80), (620, 130), (740, 190), (120, 320), (700, 350), (220, 480)]
    for sx, sy in stars:
        draw.ellipse([sx, sy, sx + 4, sy + 4], fill=(255, 255, 255, 200))
        
    draw_status_bar(draw, dark_icons=False)
    draw_top_bar(img, "Київ, Україна", "Оновлено: 22:15 (WeatherMetro)", dark_theme=True)
    
    # Hero Section
    f_moon = get_font(84)
    draw.text((W // 2 - 46, 170), "🌙", fill=(226, 232, 240, 255), font=f_moon)
    
    f_temp = get_font(120, bold=True)
    draw.text((W // 2 - 145, 270), "+14°", fill=(248, 250, 252, 255), font=f_temp)
    
    f_cond = get_font(34, bold=True)
    tb_c = draw.textbbox((0, 0), "Ясна ніч, сухо", font=f_cond)
    draw.text((W // 2 - (tb_c[2]-tb_c[0]) // 2, 410), "Ясна ніч, сухо", fill=(226, 232, 240, 255), font=f_cond)
    
    f_sub = get_font(22, semibold=True)
    sub_txt = "Відчувається як +13°  •  Макс: +22°  Мін: +11°"
    tb_s = draw.textbbox((0, 0), sub_txt, font=f_sub)
    draw.text((W // 2 - (tb_s[2]-tb_s[0]) // 2, 460), sub_txt, fill=(148, 163, 184, 255), font=f_sub)
    
    # 24h Hourly Forecast Row
    card_bg = (30, 41, 59, 210)
    card_border = (71, 85, 105, 160)
    draw_card(img, (24, 520, W - 24, 730), radius=24, fill_rgba=card_bg, outline_rgba=card_border, outline_width=2)
    
    draw.text((48, 540), "ПОГОДИННИЙ ПРОГНОЗ", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    
    hourly = [
        ("Зараз", "🌙", "+14°", "0%"),
        ("23:00", "🌙", "+13°", "0%"),
        ("00:00", "🌙", "+12°", "0%"),
        ("01:00", "☁️", "+11°", "5%"),
        ("02:00", "☁️", "+11°", "10%"),
    ]
    col_w = (W - 96) // 5
    for i, (time_str, icon, temp_str, pop_str) in enumerate(hourly):
        hx = 48 + i * col_w + col_w // 2
        f_h = get_font(20, bold=(i == 0))
        t_fill = (96, 165, 250, 255) if i == 0 else (148, 163, 184, 255)
        tb = draw.textbbox((0, 0), time_str, font=f_h)
        draw.text((hx - (tb[2]-tb[0])//2, 580), time_str, fill=t_fill, font=f_h)
        
        draw.text((hx - 18, 616), icon, fill=(226, 232, 240, 255), font=get_font(32))
        
        f_t = get_font(22, bold=True)
        tb_t = draw.textbbox((0, 0), temp_str, font=f_t)
        draw.text((hx - (tb_t[2]-tb_t[0])//2, 660), temp_str, fill=(248, 250, 252, 255), font=f_t)
        
        f_p = get_font(16)
        tb_p = draw.textbbox((0, 0), pop_str, font=f_p)
        draw.text((hx - (tb_p[2]-tb_p[0])//2, 692), pop_str, fill=(96, 165, 250, 255), font=f_p)

    # 2x2 Weather Metrics Grid
    metrics = [
        ("Вологість", "💧 68%", "Точка роси: 8°", 24, 750, W // 2 - 12, 880),
        ("Вітер", "💨 2.1 м/с", "Північний, легкий", W // 2 + 12, 750, W - 24, 880),
        ("Тиск", "🧭 1020 гПа", "765 мм рт.ст. (стабільний)", 24, 900, W // 2 - 12, 1030),
        ("УФ-індекс", "🌙 0 нічний", "Сонце зайшло о 20:18", W // 2 + 12, 900, W - 24, 1030),
    ]
    for title, val, sub, x1, y1, x2, y2 in metrics:
        draw_card(img, (x1, y1, x2, y2), radius=20, fill_rgba=card_bg, outline_rgba=card_border, outline_width=2)
        draw.text((x1 + 24, y1 + 18), title, fill=(148, 163, 184, 255), font=get_font(18, bold=True))
        draw.text((x1 + 24, y1 + 48), val, fill=(248, 250, 252, 255), font=get_font(26, bold=True))
        draw.text((x1 + 24, y1 + 90), sub, fill=(148, 163, 184, 255), font=get_font(18))
        
    # Night Arc / Moon Phase Card
    draw_card(img, (24, 1050, W - 24, 1310), radius=24, fill_rgba=card_bg, outline_rgba=card_border, outline_width=2)
    draw.text((48, 1070), "ФАЗА МІСЯЦЯ ТА НІЧ", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    
    arc_box = [120, 1100, W - 120, 1320]
    draw.arc(arc_box, start=180, end=0, fill=(71, 85, 105, 180), width=6)
    moon_x = int(W/2 + 250 * math.cos(math.radians(290)))
    moon_y = int(1210 + 110 * math.sin(math.radians(290)))
    draw.ellipse([moon_x - 16, moon_y - 16, moon_x + 16, moon_y + 16], fill=(226, 232, 240, 255), outline=(148, 163, 184, 255), width=3)
    
    draw.text((60, 1260), "Зростаючий серп (28%)", fill=(248, 250, 252, 255), font=get_font(20, bold=True))
    draw.text((W - 250, 1260), "Захід місяця: 01:14", fill=(148, 163, 184, 255), font=get_font(20))
    
    # Attribution
    f_att = get_font(16)
    draw.text((W // 2 - 160, 1420), "Погодні дані від WeatherMetro (weathermetro.com)", fill=(148, 163, 184, 255), font=f_att)
    
    draw_admob_banner(img, dark_theme=True)
    draw_nav_bar(img, active_tab_index=0, dark_theme=True)
    
    path = os.path.join(OUTPUT_DIR, "02_now_screen_dark.png")
    img.save(path, "PNG")
    print(f"Saved: {path}")

# ==========================================================
# 3. SCREEN 3: FORECAST 16 DAYS
# ==========================================================
def render_forecast_screen_16days():
    img = create_gradient(W, H, (15, 23, 42), (30, 41, 59))
    draw = ImageDraw.Draw(img)
    draw_status_bar(draw, dark_icons=False)
    draw_top_bar(img, "Прогноз на 16 днів", "Київ, Україна", dark_theme=True)
    
    card_bg = (30, 41, 59, 230)
    card_border = (71, 85, 105, 160)
    draw_card(img, (24, 160, W - 24, 490), radius=24, fill_rgba=card_bg, outline_rgba=card_border, outline_width=2)
    
    draw.text((48, 180), "ТЕМПЕРАТУРНИЙ ТРЕНД (16 ДНІВ)", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    draw.text((W - 230, 180), "🟠 Макс   🔵 Мін", fill=(203, 213, 225, 255), font=get_font(18))
    
    chart_x1, chart_x2 = 60, W - 60
    chart_y_top, chart_y_bot = 230, 420
    
    max_temps = [24, 22, 25, 26, 23, 19, 18, 20, 22, 23, 24, 21, 19, 18, 20, 22]
    min_temps = [13, 14, 15, 16, 13, 11, 10, 11, 12, 13, 14, 12, 10,  9, 11, 12]
    days_labels = ["Сб", "Нд", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд"]
    
    step_x = (chart_x2 - chart_x1) / 15
    max_pts = []
    min_pts = []
    for i in range(16):
        px = chart_x1 + i * step_x
        py_max = chart_y_bot - (max_temps[i] - 8) / (30 - 8) * (chart_y_bot - chart_y_top)
        py_min = chart_y_bot - (min_temps[i] - 8) / (30 - 8) * (chart_y_bot - chart_y_top)
        max_pts.append((px, py_max))
        min_pts.append((px, py_min))
        
    draw.line(max_pts, fill=(249, 115, 22, 255), width=4)
    draw.line(min_pts, fill=(56, 189, 248, 255), width=4)
    
    # Scrubbing Indicator at index 2
    scrub_x = max_pts[2][0]
    draw.line([(scrub_x, chart_y_top - 10), (scrub_x, chart_y_bot + 10)], fill=(255, 255, 255, 180), width=2)
    draw.ellipse([scrub_x - 7, max_pts[2][1] - 7, scrub_x + 7, max_pts[2][1] + 7], fill=(249, 115, 22, 255), outline=(255, 255, 255, 255), width=2)
    draw.ellipse([scrub_x - 7, min_pts[2][1] - 7, scrub_x + 7, min_pts[2][1] + 7], fill=(56, 189, 248, 255), outline=(255, 255, 255, 255), width=2)
    
    draw.rounded_rectangle([scrub_x - 85, chart_y_top - 50, scrub_x + 85, chart_y_top - 10], radius=10, fill=(15, 23, 42, 240), outline=(96, 165, 250, 200), width=1)
    draw.text((scrub_x - 75, chart_y_top - 44), "Пн, 25 лист: +25° / +15°", fill=(255, 255, 255, 255), font=get_font(14, bold=True))
    
    for i in range(16):
        px = chart_x1 + i * step_x
        f_lbl = get_font(14, bold=(i == 2))
        lbl_col = (255, 255, 255, 255) if i == 2 else (148, 163, 184, 255)
        draw.text((px - 10, chart_y_bot + 18), days_labels[i], fill=lbl_col, font=f_lbl)

    # 16-Day List View
    draw_card(img, (24, 510, W - 24, 1420), radius=24, fill_rgba=card_bg, outline_rgba=card_border, outline_width=2)
    draw.text((48, 530), "ПРОГНОЗ ПО ДНЯХ (ДЕТАЛЬНО)", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    
    days_data = [
        ("Сьогодні, 23 лист.", "☀️", "Ясно, без опадів", "+13°", "+24°", 0.7, 0.95),
        ("Завтра, 24 лист.", "🌤️", "Мінлива хмарність", "+14°", "+22°", 0.65, 0.88),
        ("Пн, 25 лист.", "☀️", "Сонячно, тепло", "+15°", "+25°", 0.72, 1.0),
        ("Вт, 26 лист.", "🌤️", "Сухо, вітер 3 м/с", "+16°", "+26°", 0.75, 1.02),
        ("Ср, 27 лист.", "⛅", "Хмарно з проясненнями", "+13°", "+23°", 0.62, 0.9),
        ("Чт, 28 лист.", "🌧️", "Короткочасний дощ 80%", "+11°", "+19°", 0.5, 0.76),
        ("Пт, 29 лист.", "🌧️", "Злива, 12 мм опадів", "+10°", "+18°", 0.45, 0.72),
        ("Сб, 30 лист.", "⛅", "Прояснення, свіжо", "+11°", "+20°", 0.5, 0.8),
    ]
    
    row_y = 575
    for day, icon, cond, min_t, max_t, bar_start, bar_end in days_data:
        draw.text((48, row_y + 12), day, fill=(248, 250, 252, 255), font=get_font(20, bold=True))
        draw.text((250, row_y + 8), icon, fill=(255, 215, 0, 255), font=get_font(26))
        draw.text((290, row_y + 14), cond[:20], fill=(148, 163, 184, 255), font=get_font(18))
        
        draw.text((490, row_y + 14), min_t, fill=(56, 189, 248, 255), font=get_font(20, bold=True))
        
        bar_bg_x1, bar_bg_x2 = 550, 700
        draw.rounded_rectangle([bar_bg_x1, row_y + 20, bar_bg_x2, row_y + 30], radius=5, fill=(51, 65, 85, 180))
        
        fill_x1 = int(bar_bg_x1 + (bar_bg_x2 - bar_bg_x1) * (bar_start * 0.8))
        fill_x2 = int(bar_bg_x1 + (bar_bg_x2 - bar_bg_x1) * min(1.0, (bar_end * 0.95)))
        draw.rounded_rectangle([fill_x1, row_y + 20, fill_x2, row_y + 30], radius=5, fill=(249, 115, 22, 255))
        
        draw.text((715, row_y + 14), max_t, fill=(249, 115, 22, 255), font=get_font(20, bold=True))
        draw.line([(48, row_y + 58), (W - 48, row_y + 58)], fill=(71, 85, 105, 100), width=1)
        row_y += 64

    f_att = get_font(16)
    draw.text((W // 2 - 160, 1435), "Погодні дані від WeatherMetro (weathermetro.com)", fill=(148, 163, 184, 255), font=f_att)

    draw_admob_banner(img, dark_theme=True)
    draw_nav_bar(img, active_tab_index=1, dark_theme=True)
    
    path = os.path.join(OUTPUT_DIR, "03_forecast_screen_16days.png")
    img.save(path, "PNG")
    print(f"Saved: {path}")

# ==========================================================
# 4. SCREEN 4: LOCATIONS SCREEN
# ==========================================================
def render_locations_screen():
    img = create_gradient(W, H, (15, 23, 42), (30, 41, 59))
    draw = ImageDraw.Draw(img)
    draw_status_bar(draw, dark_icons=False)
    draw_top_bar(img, "Збережені місця", None, dark_theme=True)
    
    draw_card(img, (24, 160, W - 24, 230), radius=20, fill_rgba=(30, 41, 59, 240), outline_rgba=(96, 165, 250, 180), outline_width=2)
    draw.text((48, 180), "🔍", fill=(96, 165, 250, 255), font=get_font(28))
    draw.text((96, 184), "Пошук міста або індексу...", fill=(148, 163, 184, 255), font=get_font(22))
    
    draw_card(img, (24, 250, W - 24, 320), radius=18, fill_rgba=(37, 99, 235, 40), outline_rgba=(37, 99, 235, 180), outline_width=2)
    draw.text((48, 270), "📍", fill=(96, 165, 250, 255), font=get_font(28))
    draw.text((96, 272), "Визначити поточну локацію (GPS за запитом)", fill=(248, 250, 252, 255), font=get_font(20, bold=True))
    
    draw.text((36, 350), "ЗБЕРЕЖЕНІ МІСТА (4)", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    
    card_bg = (30, 41, 59, 230)
    card_border = (71, 85, 105, 160)
    
    # City 1: Kyiv (ACTIVE)
    draw_card(img, (24, 380, W - 24, 520), radius=22, fill_rgba=card_bg, outline_rgba=(59, 130, 246, 220), outline_width=2)
    draw.text((48, 404), "📍 Київ, Україна", fill=(248, 250, 252, 255), font=get_font(26, bold=True))
    draw.rounded_rectangle([270, 404, 390, 434], radius=8, fill=(37, 99, 235, 240))
    draw.text((284, 408), "АКТИВНЕ", fill=(255, 255, 255, 255), font=get_font(16, bold=True))
    
    draw.text((48, 454), "☀️ Ясно, без опадів  •  Макс: +24°  Мін: +13°", fill=(148, 163, 184, 255), font=get_font(18))
    draw.text((W - 140, 420), "+21°", fill=(248, 250, 252, 255), font=get_font(52, bold=True))

    # City 2: Warsaw
    draw_card(img, (24, 540, W - 24, 680), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    draw.text((48, 564), "Варшава, Польща", fill=(248, 250, 252, 255), font=get_font(26, bold=True))
    draw.text((48, 614), "🌤️ Хмарно з проясненнями  •  Макс: +19°  Мін: +10°", fill=(148, 163, 184, 255), font=get_font(18))
    draw.text((W - 140, 580), "+18°", fill=(248, 250, 252, 255), font=get_font(52, bold=True))

    # City 3: Lviv (Swipe-to-Dismiss action simulation)
    draw.rounded_rectangle([24, 700, W - 24, 840], radius=22, fill=(220, 38, 38, 240))
    draw.text((W - 170, 750), "🗑️ Видалити", fill=(255, 255, 255, 255), font=get_font(22, bold=True))
    draw_card(img, (24 - 180, 700, W - 24 - 180, 840), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    draw.text((48 - 180, 724), "Львів, Україна", fill=(248, 250, 252, 255), font=get_font(26, bold=True))
    draw.text((48 - 180, 774), "🌧️ Дощ 70%  •  Макс: +16°  Мін: +8°", fill=(148, 163, 184, 255), font=get_font(18))
    draw.text((W - 140 - 180, 740), "+14°", fill=(248, 250, 252, 255), font=get_font(52, bold=True))

    # City 4: Berlin
    draw_card(img, (24, 860, W - 24, 1000), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    draw.text((48, 884), "Берлін, Німеччина", fill=(248, 250, 252, 255), font=get_font(26, bold=True))
    draw.text((48, 934), "⛅ Мінлива хмарність  •  Макс: +17°  Мін: +9°", fill=(148, 163, 184, 255), font=get_font(18))
    draw.text((W - 140, 900), "+16°", fill=(248, 250, 252, 255), font=get_font(52, bold=True))

    snack_y = 1380
    draw.rounded_rectangle([24, snack_y, W - 24, snack_y + 70], radius=16, fill=(15, 23, 42, 250), outline=(59, 130, 246, 200), width=2)
    draw.text((44, snack_y + 22), "Місто 'Львів' видалено", fill=(248, 250, 252, 255), font=get_font(20))
    draw.text((W - 220, snack_y + 22), "СКАСУВАТИ (4с)", fill=(96, 165, 250, 255), font=get_font(20, bold=True))

    draw_admob_banner(img, dark_theme=True)
    draw_nav_bar(img, active_tab_index=2, dark_theme=True)
    
    path = os.path.join(OUTPUT_DIR, "04_locations_screen.png")
    img.save(path, "PNG")
    print(f"Saved: {path}")

# ==========================================================
# 5. SCREEN 5: SETTINGS SCREEN
# ==========================================================
def render_settings_screen():
    img = create_gradient(W, H, (15, 23, 42), (30, 41, 59))
    draw = ImageDraw.Draw(img)
    draw_status_bar(draw, dark_icons=False)
    draw_top_bar(img, "Налаштування", None, dark_theme=True, show_back=True)
    
    card_bg = (30, 41, 59, 230)
    card_border = (71, 85, 105, 160)
    
    # 1. Units Section
    draw.text((36, 170), "ОДИНИЦІ ВИМІРУ", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    draw_card(img, (24, 200, W - 24, 470), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    
    draw.text((48, 226), "Температура", fill=(248, 250, 252, 255), font=get_font(22, bold=True))
    draw.rounded_rectangle([W - 240, 220, W - 48, 264], radius=12, fill=(15, 23, 42, 200), outline=(71, 85, 105, 180), width=1)
    draw.rounded_rectangle([W - 238, 222, W - 146, 262], radius=10, fill=(37, 99, 235, 255))
    draw.text((W - 206, 230), "°C", fill=(255, 255, 255, 255), font=get_font(20, bold=True))
    draw.text((W - 104, 230), "°F", fill=(148, 163, 184, 255), font=get_font(20))
    
    draw.text((48, 316), "Швидкість вітру", fill=(248, 250, 252, 255), font=get_font(22, bold=True))
    draw.rounded_rectangle([W - 280, 310, W - 48, 354], radius=12, fill=(15, 23, 42, 200), outline=(71, 85, 105, 180), width=1)
    draw.rounded_rectangle([W - 278, 312, W - 170, 352], radius=10, fill=(37, 99, 235, 255))
    draw.text((W - 244, 320), "м/с", fill=(255, 255, 255, 255), font=get_font(20, bold=True))
    draw.text((W - 130, 320), "км/год", fill=(148, 163, 184, 255), font=get_font(18))

    draw.text((48, 406), "Атмосферний тиск", fill=(248, 250, 252, 255), font=get_font(22, bold=True))
    draw.rounded_rectangle([W - 300, 400, W - 48, 444], radius=12, fill=(15, 23, 42, 200), outline=(71, 85, 105, 180), width=1)
    draw.rounded_rectangle([W - 298, 402, W - 180, 442], radius=10, fill=(37, 99, 235, 255))
    draw.text((W - 262, 410), "гПа", fill=(255, 255, 255, 255), font=get_font(20, bold=True))
    draw.text((W - 156, 410), "мм рт.ст.", fill=(148, 163, 184, 255), font=get_font(18))

    # 2. Languages Section
    draw.text((36, 510), "МОВА ТА РЕГІОН", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    draw_card(img, (24, 540, W - 24, 660), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    draw.text((48, 564), "Мова застосунку", fill=(248, 250, 252, 255), font=get_font(22, bold=True))
    draw.text((48, 608), "Підтримується 8 мов (UK, PL, EN, DE, ES, IT, RU + Default)", fill=(148, 163, 184, 255), font=get_font(16))
    draw.rounded_rectangle([W - 250, 560, W - 48, 606], radius=12, fill=(15, 23, 42, 200), outline=(71, 85, 105, 180), width=1)
    draw.text((W - 232, 570), "🇺🇦 Українська ▼", fill=(248, 250, 252, 255), font=get_font(18, bold=True))

    # 3. Data Providers & Licenses
    draw.text((36, 700), "ДЖЕРЕЛА ДАНИХ ТА ЛІЦЕНЗІЇ", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    draw_card(img, (24, 730, W - 24, 950), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    draw.text((48, 754), "Основне джерело (Commercial Free):", fill=(148, 163, 184, 255), font=get_font(16))
    draw.text((48, 782), "WeatherMetro Free API (100k викликів/міс)", fill=(248, 250, 252, 255), font=get_font(22, bold=True))
    draw.text((48, 816), "Вимога: обов'язкове посилання 'Погодні дані від WeatherMetro'", fill=(96, 165, 250, 255), font=get_font(16))
    
    draw.line([(48, 850), (W - 48, 850)], fill=(71, 85, 105, 100), width=1)
    
    draw.text((48, 868), "Резервне джерело (Non-commercial Fallback):", fill=(148, 163, 184, 255), font=get_font(16))
    draw.text((48, 896), "Open-Meteo Free API (Ліцензія CC BY 4.0)", fill=(248, 250, 252, 255), font=get_font(22, bold=True))

    # 4. Privacy & Permissions Section
    draw.text((36, 990), "ПРИВАТНІСТЬ ТА БЕЗПЕКА ДАНИХ", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    draw_card(img, (24, 1020, W - 24, 1180), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    draw.text((48, 1044), "Доступ до геолокації (GPS)", fill=(248, 250, 252, 255), font=get_font(22, bold=True))
    draw.text((48, 1076), "Виключно за явної дії користувача у Foreground.", fill=(148, 163, 184, 255), font=get_font(16))
    draw.text((48, 1104), "ACCESS_BACKGROUND_LOCATION: Суворо заборонено (0% витоків).", fill=(34, 197, 94, 255), font=get_font(16, bold=True))
    draw.text((48, 1132), "Штучний псевдорадар: Виключено з MVP (No Fake Data Policy).", fill=(96, 165, 250, 255), font=get_font(16))

    # 5. About App & Legal
    draw_card(img, (24, 1220, W - 24, 1420), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    draw.text((48, 1244), "Погода — Smart Forecast v1.0.0 (Release 14)", fill=(248, 250, 252, 255), font=get_font(20, bold=True))
    draw.text((48, 1276), "Бюджет розробки: $0.00 • Google AdMob • Glance AppWidgets", fill=(148, 163, 184, 255), font=get_font(16))
    
    draw.text((48, 1324), "Політика конфіденційності", fill=(96, 165, 250, 255), font=get_font(20, bold=True))
    draw.text((W - 80, 1324), "→", fill=(96, 165, 250, 255), font=get_font(22, bold=True))
    draw.text((48, 1368), "Ліцензії відкритого коду (Open Source)", fill=(96, 165, 250, 255), font=get_font(20, bold=True))
    draw.text((W - 80, 1368), "→", fill=(96, 165, 250, 255), font=get_font(22, bold=True))

    draw_admob_banner(img, dark_theme=True)
    draw_nav_bar(img, active_tab_index=2, dark_theme=True)
    
    path = os.path.join(OUTPUT_DIR, "05_settings_screen.png")
    img.save(path, "PNG")
    print(f"Saved: {path}")

# ==========================================================
# 6. SCREEN 6: DAY DETAILS BOTTOM SHEET
# ==========================================================
def render_day_details_sheet():
    base = create_gradient(W, H, (10, 17, 40), (30, 41, 59))
    base = base.filter(ImageFilter.GaussianBlur(radius=8))
    
    dim = Image.new("RGBA", (W, H), (0, 0, 0, 160))
    base.alpha_composite(dim)
    
    sheet_y = 500
    draw_card(base, (0, sheet_y, W, H), radius=36, fill_rgba=(15, 23, 42, 250), outline_rgba=(96, 165, 250, 180), outline_width=2)
    draw = ImageDraw.Draw(base)
    
    draw.rounded_rectangle([W // 2 - 50, sheet_y + 16, W // 2 + 50, sheet_y + 24], radius=4, fill=(148, 163, 184, 180))
    
    draw.text((48, sheet_y + 44), "Субота, 23 листопада", fill=(248, 250, 252, 255), font=get_font(32, bold=True))
    draw.text((48, sheet_y + 90), "Київ, Україна • Детальний 24-годинний огляд", fill=(148, 163, 184, 255), font=get_font(18))
    
    draw.rounded_rectangle([W - 90, sheet_y + 44, W - 36, sheet_y + 98], radius=16, fill=(51, 65, 85, 180))
    draw.text((W - 74, sheet_y + 54), "✕", fill=(248, 250, 252, 255), font=get_font(26, bold=True))
    
    card_bg = (30, 41, 59, 230)
    card_border = (71, 85, 105, 160)
    draw_card(base, (24, sheet_y + 140, W - 24, sheet_y + 300), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    
    draw.text((48, sheet_y + 160), "☀️", fill=(255, 215, 0, 255), font=get_font(56))
    draw.text((130, sheet_y + 164), "+21°", fill=(248, 250, 252, 255), font=get_font(52, bold=True))
    draw.text((250, sheet_y + 172), "Ясно, без істотних опадів", fill=(248, 250, 252, 255), font=get_font(22, bold=True))
    draw.text((130, sheet_y + 230), "Денний інтервал: +13° ... +24°  •  Ймовірність дощу: 0%", fill=(148, 163, 184, 255), font=get_font(18))

    draw.text((36, sheet_y + 330), "ПОГОДИННИЙ РОЗРІЗ ДНЯ", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    draw_card(base, (24, sheet_y + 360, W - 24, sheet_y + 700), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    
    hours_rows = [
        ("06:00", "🌅 Схід", "+13°", "0%", "💨 2.1 м/с", "💧 82%"),
        ("09:00", "☀️ Ясно", "+17°", "0%", "💨 3.4 м/с", "💧 65%"),
        ("12:00", "☀️ Тепло", "+22°", "0%", "💨 4.5 м/с", "💧 48%"),
        ("15:00", "☀️ Пік тепла", "+24°", "0%", "💨 4.8 м/с", "💧 42%"),
        ("18:00", "🌤️ Прохолода", "+20°", "5%", "💨 3.2 м/с", "💧 54%"),
    ]
    ry = sheet_y + 380
    for hr, cond, temp, pop, wind, hum in hours_rows:
        draw.text((48, ry), hr, fill=(248, 250, 252, 255), font=get_font(20, bold=True))
        draw.text((150, ry), cond, fill=(248, 250, 252, 255), font=get_font(20))
        draw.text((340, ry), temp, fill=(249, 115, 22, 255), font=get_font(20, bold=True))
        draw.text((440, ry), pop, fill=(96, 165, 250, 255), font=get_font(18))
        draw.text((540, ry), wind, fill=(148, 163, 184, 255), font=get_font(18))
        draw.text((680, ry), hum, fill=(148, 163, 184, 255), font=get_font(18))
        draw.line([(48, ry + 44), (W - 48, ry + 44)], fill=(71, 85, 105, 80), width=1)
        ry += 60

    draw_card(base, (24, sheet_y + 730, W // 2 - 12, sheet_y + 890), radius=20, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    draw.text((48, sheet_y + 750), "УФ-ІНДЕКС", fill=(148, 163, 184, 255), font=get_font(16, bold=True))
    draw.text((48, sheet_y + 780), "5.4 Помірний", fill=(251, 191, 36, 255), font=get_font(24, bold=True))
    draw.text((48, sheet_y + 825), "Пік о 13:00. Рекомендовано сонцезахисні окуляри.", fill=(148, 163, 184, 255), font=get_font(15))

    draw_card(base, (W // 2 + 12, sheet_y + 730, W - 24, sheet_y + 890), radius=20, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    draw.text((W // 2 + 36, sheet_y + 750), "ТИСК ТА ВІТЕР", fill=(148, 163, 184, 255), font=get_font(16, bold=True))
    draw.text((W // 2 + 36, sheet_y + 780), "1018 гПа / Пн-Сх", fill=(248, 250, 252, 255), font=get_font(24, bold=True))
    draw.text((W // 2 + 36, sheet_y + 825), "Пориви вітру до 7.5 м/с у денні години.", fill=(148, 163, 184, 255), font=get_font(15))

    draw.rounded_rectangle([48, sheet_y + 930, W - 48, sheet_y + 1010], radius=18, fill=(37, 99, 235, 255))
    draw.text((W // 2 - 80, sheet_y + 954), "Закрити деталі", fill=(255, 255, 255, 255), font=get_font(22, bold=True))

    path = os.path.join(OUTPUT_DIR, "06_day_details_sheet.png")
    base.save(path, "PNG")
    print(f"Saved: {path}")

# ==========================================================
# 7. SCREEN 7: GLANCE WIDGETS 2x2 AND 4x2
# ==========================================================
def render_glance_widgets():
    img = create_gradient(W, H, (18, 28, 54), (10, 15, 30))
    draw = ImageDraw.Draw(img)
    draw_status_bar(draw, dark_icons=False)
    
    draw.ellipse([-100, 700, 950, 1750], fill=(22, 33, 62, 255))
    draw.ellipse([-50, 900, 900, 1850], fill=(15, 23, 42, 255))
    
    draw.rounded_rectangle([40, 70, W - 40, 140], radius=16, fill=(15, 23, 42, 220), outline=(96, 165, 250, 180), width=1)
    draw.text((60, 85), "ДОМАШНІЙ ЕКРАН ANDROID • JETPACK GLANCE APPWIDGETS", fill=(96, 165, 250, 255), font=get_font(18, bold=True))
    draw.text((60, 110), "Glassmorphism 135° • RemoteViews Compliant • Оновлення раз на 15 хв • 0% Реклами", fill=(203, 213, 225, 255), font=get_font(14))

    # WIDGET 1: 2x2
    w2_x1, w2_y1, w2_x2, w2_y2 = 40, 180, 420, 560
    draw_card(img, (w2_x1, w2_y1, w2_x2, w2_y2), radius=32, fill_rgba=(35, 45, 72, 235), outline_rgba=(255, 255, 255, 60), outline_width=2)
    
    draw.text((w2_x1 + 24, w2_y1 + 24), "📍 Київ", fill=(248, 250, 252, 255), font=get_font(24, bold=True))
    draw.text((w2_x2 - 80, w2_y1 + 24), "14:30", fill=(148, 163, 184, 255), font=get_font(20))
    
    draw.text((w2_x1 + 30, w2_y1 + 80), "☀️", fill=(255, 215, 0, 255), font=get_font(68))
    draw.text((w2_x1 + 130, w2_y1 + 85), "+21°", fill=(248, 250, 252, 255), font=get_font(64, bold=True))
    
    draw.text((w2_x1 + 24, w2_y1 + 200), "Ясно, без опадів", fill=(226, 232, 240, 255), font=get_font(22, bold=True))
    draw.text((w2_x1 + 24, w2_y1 + 240), "Макс: +24°  •  Мін: +13°", fill=(148, 163, 184, 255), font=get_font(18))
    draw.text((w2_x1 + 24, w2_y1 + 320), "WeatherMetro Free", fill=(96, 165, 250, 200), font=get_font(14))

    draw.text((450, 260), "← Віджет 2x2 (Компактний)", fill=(255, 255, 255, 255), font=get_font(22, bold=True))
    draw.text((450, 300), "• Розмір: 2x2 сітки робочого столу\n• Поточна температура й стан\n• Денний діапазон мін/макс\n• Відкриття застосунку в 1 тап", fill=(203, 213, 225, 255), font=get_font(16))

    # WIDGET 2: 4x2
    w4_x1, w4_y1, w4_x2, w4_y2 = 40, 620, W - 40, 1000
    draw_card(img, (w4_x1, w4_y1, w4_x2, w4_y2), radius=32, fill_rgba=(35, 45, 72, 235), outline_rgba=(255, 255, 255, 60), outline_width=2)
    
    draw.text((w4_x1 + 28, w4_y1 + 24), "📍 Київ, Україна", fill=(248, 250, 252, 255), font=get_font(24, bold=True))
    draw.text((w4_x1 + 240, w4_y1 + 26), "14:30", fill=(148, 163, 184, 255), font=get_font(18))
    
    draw.text((w4_x1 + 28, w4_y1 + 75), "☀️", fill=(255, 215, 0, 255), font=get_font(60))
    draw.text((w4_x1 + 120, w4_y1 + 75), "+21°", fill=(248, 250, 252, 255), font=get_font(64, bold=True))
    draw.text((w4_x1 + 28, w4_y1 + 170), "Ясно, без опадів", fill=(226, 232, 240, 255), font=get_font(20, bold=True))
    draw.text((w4_x1 + 28, w4_y1 + 210), "Відчув. як +22°  •  Макс: +24° / Мін: +13°", fill=(148, 163, 184, 255), font=get_font(16))
    draw.text((w4_x1 + 28, w4_y1 + 250), "💧 45%   💨 4.2 м/с   🧭 1018 гПа", fill=(203, 213, 225, 255), font=get_font(18))
    draw.text((w4_x1 + 28, w4_y1 + 330), "Дані: WeatherMetro (Оновлено 14:30)", fill=(96, 165, 250, 200), font=get_font(14))

    div_x = w4_x1 + 360
    draw.line([(div_x, w4_y1 + 24), (div_x, w4_y2 - 24)], fill=(71, 85, 105, 120), width=1)

    draw.text((div_x + 24, w4_y1 + 24), "ПРОГНОЗ НА 4 ДНІ", fill=(148, 163, 184, 255), font=get_font(16, bold=True))
    
    forecast_rows = [
        ("Сб", "🌤️", "+22° / +14°", "20% 💧"),
        ("Нд", "🌧️", "+18° / +11°", "85% 💧"),
        ("Пн", "⛅", "+19° / +10°", "15% 💧"),
        ("Вт", "☀️", "+23° / +12°", "0% 💧"),
    ]
    fry = w4_y1 + 75
    for day, icn, temps, pop in forecast_rows:
        draw.text((div_x + 24, fry), day, fill=(248, 250, 252, 255), font=get_font(20, bold=True))
        draw.text((div_x + 70, fry - 4), icn, fill=(255, 215, 0, 255), font=get_font(24))
        draw.text((div_x + 120, fry), temps, fill=(248, 250, 252, 255), font=get_font(18))
        draw.text((div_x + 270, fry), pop, fill=(96, 165, 250, 255), font=get_font(18, bold=True))
        draw.line([(div_x + 24, fry + 44), (w4_x2 - 24, fry + 44)], fill=(71, 85, 105, 60), width=1)
        fry += 62

    draw.text((40, 1040), "↑ Віджет 4x2 (Розширений)", fill=(255, 255, 255, 255), font=get_font(22, bold=True))
    draw.text((40, 1080), "• Двоколонковий збалансований дизайн (Left: поточні метеопоказники, Right: 4-денний прогноз)\n• Оптимізовано для економії заряду батареї (AlarmManager / WorkManager 15-30 хв)\n• Нуль покадрових анімацій відповідно до системних лімітів RemoteViews\n• Суворо 0% реклами (повна відповідність Google Play та AdMob Policy)", fill=(203, 213, 225, 255), font=get_font(16))

    dock_y = 1620
    draw_card(img, (40, dock_y, W - 40, dock_y + 130), radius=36, fill_rgba=(30, 41, 59, 180), outline_rgba=(255, 255, 255, 30), outline_width=1)
    
    dock_icons = [("📞", "Телефон"), ("💬", "Повідомлення"), ("☀️", "Погода"), ("🌐", "Браузер"), ("📷", "Камера")]
    dock_w = (W - 80) // 5
    for i, (icn, nm) in enumerate(dock_icons):
        dx = 40 + i * dock_w + dock_w // 2
        draw.text((dx - 22, dock_y + 24), icn, fill=(255, 255, 255, 255), font=get_font(42))
        f_d = get_font(14)
        tb = draw.textbbox((0, 0), nm, font=f_d)
        draw.text((dx - (tb[2]-tb[0])//2, dock_y + 84), nm, fill=(203, 213, 225, 255), font=f_d)

    path = os.path.join(OUTPUT_DIR, "07_glance_widgets_2x2_4x2.png")
    img.save(path, "PNG")
    print(f"Saved: {path}")

# ==========================================================
# 8. SCREEN 8: ADMOB ANCHORED ADAPTIVE BANNER SPECS
# ==========================================================
def render_admob_adaptive_banner():
    img = create_gradient(W, H, (15, 23, 42), (30, 41, 59))
    draw = ImageDraw.Draw(img)
    draw_status_bar(draw, dark_icons=False)
    draw_top_bar(img, "AdMob Adaptive Banner", "Технічна специфікація Zero CLS", dark_theme=True, show_back=True)
    
    card_bg = (30, 41, 59, 230)
    card_border = (71, 85, 105, 160)
    
    draw_card(img, (24, 160, W - 24, 340), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    draw.text((48, 184), "АРХІТЕКТУРА РОЗМІЩЕННЯ РЕКЛАМИ ADMOB", fill=(96, 165, 250, 255), font=get_font(18, bold=True))
    draw.text((48, 220), "• Google Anchored Adaptive Banner: динамічна висота 50-90dp залежно від ширини екрана\n• Zero CLS (Cumulative Layout Shift = 0): Compose резервує точний Box контейнер до завантаження\n• Безпечна відстань (16dp): відступ від BottomNavigationBar запобігає випадковим клікам\n• Віджети Android: категорично 0% реклами відповідно до політики AdMob", fill=(203, 213, 225, 255), font=get_font(18))

    draw.text((36, 380), "СТАН 1: РЕЗЕРВУВАННЯ КОНТЕЙНЕРА (SKELETON SHIMMER / ZERO CLS)", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    
    box1_y = 420
    draw.rounded_rectangle([24, box1_y, W - 24, box1_y + 180], radius=20, fill=(15, 23, 42, 200), outline=(59, 130, 246, 200), width=2)
    draw.text((48, box1_y + 16), "Compose Box: height = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize().getHeight()", fill=(96, 165, 250, 255), font=get_font(16, bold=True))
    
    skel_y = box1_y + 50
    draw.rounded_rectangle([44, skel_y, 160, skel_y + 40], radius=8, fill=(51, 65, 85, 200))
    draw.rounded_rectangle([44, skel_y + 52, 450, skel_y + 80], radius=6, fill=(51, 65, 85, 160))
    draw.rounded_rectangle([W - 200, skel_y + 10, W - 44, skel_y + 80], radius=14, fill=(51, 65, 85, 220))
    draw.text((W - 145, skel_y + 35), "AD", fill=(148, 163, 184, 255), font=get_font(20, bold=True))

    draw.text((36, 650), "СТАН 2: ЗАВАНТАЖЕНЕ ОГОЛОШЕННЯ (LIVE ANCHORED ADAPTIVE BANNER)", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    
    box2_y = 690
    draw.rounded_rectangle([24, box2_y, W - 24, box2_y + 180], radius=20, fill=(15, 23, 42, 200), outline=(34, 197, 94, 200), width=2)
    draw.text((48, box2_y + 16), "AdView успішно відрендерено без зміщення інтерфейсу (CLS = 0.00)", fill=(34, 197, 94, 255), font=get_font(16, bold=True))
    
    ad_y = box2_y + 50
    draw_card(img, (40, ad_y, W - 40, ad_y + 100), radius=16, fill_rgba=(30, 41, 59, 240), outline_rgba=(71, 85, 105, 180), outline_width=2)
    
    draw.rounded_rectangle([56, ad_y + 14, 110, ad_y + 38], radius=6, fill=(22, 163, 74, 240))
    draw.text((65, ad_y + 17), "Тест", fill=(255, 255, 255, 255), font=get_font(16, bold=True))
    
    draw.text((122, ad_y + 15), "Google AdMob • Тестовий партнерський блок", fill=(241, 245, 249, 255), font=get_font(22, bold=True))
    draw.text((56, ad_y + 54), "Повноекранне масштабування ширини • Безпечна висота 64dp", fill=(148, 163, 184, 255), font=get_font(16))
    
    draw.rounded_rectangle([W - 190, ad_y + 24, W - 56, ad_y + 76], radius=14, fill=(37, 99, 235, 255))
    draw.text((W - 172, ad_y + 38), "Встановити", fill=(255, 255, 255, 255), font=get_font(20, bold=True))

    draw.text((36, 920), "КАРТА БЕЗПЕЧНИХ ВІДСТУПІВ (ACCESSIBILITY & TOUCH SAFETY)", fill=(148, 163, 184, 255), font=get_font(18, bold=True))
    draw_card(img, (24, 960, W - 24, 1380), radius=22, fill_rgba=card_bg, outline_rgba=card_border, outline_width=1)
    
    draw.text((48, 990), "📐 Правила розміщення елементів на екрані:", fill=(248, 250, 252, 255), font=get_font(20, bold=True))
    
    specs = [
        ("Позиція банера", "Зафіксовано над BottomNavigationBar (Bottom Anchored)"),
        ("Відступ зверху", ">= 12dp від погодних карток / інформаційного контенту"),
        ("Відступ знизу", ">= 16dp від навігаційної панелі для уникнення випадкових тапів"),
        ("Висота на Compact (<600dp)", "Динамічно 50dp - 64dp (обчислюється Google Mobile Ads SDK)"),
        ("Висота на Medium (600-840dp)", "Динамічно 70dp - 80dp"),
        ("Висота на Expanded (>840dp)", "Динамічно 90dp"),
        ("Cumulative Layout Shift", "0.00 (Повна стабільність макета при першому завантаженні)"),
    ]
    sp_y = 1040
    for title, desc in specs:
        draw.text((48, sp_y), f"• {title}:", fill=(96, 165, 250, 255), font=get_font(18, bold=True))
        draw.text((340, sp_y), desc, fill=(203, 213, 225, 255), font=get_font(18))
        draw.line([(48, sp_y + 36), (W - 48, sp_y + 36)], fill=(71, 85, 105, 80), width=1)
        sp_y += 48

    draw_admob_banner(img, dark_theme=True)
    draw_nav_bar(img, active_tab_index=0, dark_theme=True)
    
    path = os.path.join(OUTPUT_DIR, "08_admob_adaptive_banner.png")
    img.save(path, "PNG")
    print(f"Saved: {path}")

if __name__ == "__main__":
    print("Starting generation of all 8 design mockups...")
    render_now_screen_light()
    render_now_screen_dark()
    render_forecast_screen_16days()
    render_locations_screen()
    render_settings_screen()
    render_day_details_sheet()
    render_glance_widgets()
    render_admob_adaptive_banner()
    print("All 8 mockups successfully rendered and saved in docs/design/previews/!")
