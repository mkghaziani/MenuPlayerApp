"""
ERPNext Custom Method: menu.get_screen_data
===========================================

Place this file at:
  /home/<bench>/apps/<your_app>/your_app/api/menu.py

Then add to hooks.py:
  override_whitelisted_methods = {}

Or simply whitelist via @frappe.whitelist():
  The full API endpoint becomes:
  GET /api/method/your_app.api.menu.get_screen_data?screen=screen-1

Usage:
  No authentication required (guest) OR token auth depending on your ERPNext settings.
  For token auth, create an API key/secret for a dedicated "Menu Player" user.
"""

import frappe
import json


@frappe.whitelist(allow_guest=True)
def get_screen_data(screen: str = "screen-1") -> dict:
    """
    Returns display data for a given screen ID.

    Expected Doctype: "Menu Screen" with child table "Menu Screen Item"
    Alternatively, adapt to your own Doctype structure.
    """

    # ── Option A: Using a custom "Menu Screen" Doctype ──────────────────────
    # Uncomment and adapt if you have a dedicated Doctype:
    #
    # if not frappe.db.exists("Menu Screen", screen):
    #     frappe.throw(f"Screen '{screen}' not found", frappe.DoesNotExistError)
    #
    # doc = frappe.get_doc("Menu Screen", screen)
    # items = []
    # for row in doc.items:
    #     item_doc = frappe.get_doc("Item", row.item_code)
    #     items.append({
    #         "name": item_doc.item_name,
    #         "price": frappe.db.get_value("Item Price", {"item_code": row.item_code}, "price_list_rate") or 0,
    #         "image": item_doc.image or "",
    #         "video": row.get("video_url") or "",
    #         "available": row.get("available", True),
    #         "category": item_doc.item_group or "",
    #         "description": item_doc.description or "",
    #         "duration": row.get("display_duration") or 5,
    #     })
    #
    # return {
    #     "screen": screen,
    #     "items": items,
    #     "settings": {
    #         "refresh_interval_sec": doc.refresh_interval or 30,
    #         "slide_duration_sec": doc.slide_duration or 5,
    #         "currency_symbol": frappe.db.get_default("currency") or "Rs",
    #         "show_price": doc.show_price,
    #         "background_color": doc.get("bg_color") or "#000000",
    #         "text_color": doc.get("text_color") or "#FFFFFF",
    #         "logo_url": doc.get("logo") or "",
    #     }
    # }

    # ── Option B: Hardcoded/demo response (use while setting up) ────────────
    base_url = frappe.utils.get_url()

    # Fetch items tagged for this screen using a custom field on Item Doctype
    # Custom field: "menu_screen_ids" (Small Text, comma-separated screen IDs)
    items_data = frappe.db.sql("""
        SELECT
            i.item_name AS name,
            IFNULL(ip.price_list_rate, 0) AS price,
            IFNULL(i.image, '') AS image,
            IFNULL(i.website_image, '') AS video,
            1 AS available,
            IFNULL(i.item_group, '') AS category,
            IFNULL(i.description, '') AS description,
            5 AS duration
        FROM `tabItem` i
        LEFT JOIN `tabItem Price` ip
            ON ip.item_code = i.name
            AND ip.selling = 1
        WHERE
            i.disabled = 0
            AND i.is_sales_item = 1
            AND (i.custom_menu_screens IS NULL
                 OR i.custom_menu_screens = ''
                 OR FIND_IN_SET(%(screen)s, i.custom_menu_screens) > 0)
        LIMIT 50
    """, {"screen": screen}, as_dict=True)

    # Fix image URLs to be absolute
    for item in items_data:
        if item.get("image") and not item["image"].startswith("http"):
            item["image"] = base_url + item["image"]
        if item.get("video") and not item["video"].startswith("http"):
            item["video"] = base_url + item["video"]
        item["available"] = bool(item.get("available"))

    return {
        "screen": screen,
        "items": items_data,
        "settings": {
            "refresh_interval_sec": 30,
            "slide_duration_sec": 5,
            "currency_symbol": frappe.db.get_default("currency") or "Rs",
            "show_price": True,
            "background_color": "#000000",
            "text_color": "#FFFFFF",
            "logo_url": ""
        }
    }
