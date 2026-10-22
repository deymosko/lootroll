# LootRoll 1.1.0 (Minecraft 1.21.1)

## ✨ New
- **Minecraft 1.21.1 support** on **NeoForge** and **Fabric**.
  Requires Architectury API (+ Fabric API and Forge Config API Port on Fabric).
- **New roll notifications:** WoW-style roll frames on the HUD with item icon,
  rarity colour and timer bar.
- **Vote hotkeys:** J / K / N for Need / Greed / Pass on the top roll, no screen needed
  (rebindable under Controls → LootRoll).
- **Reworked vote screen:** proper scrolling for many items (mouse wheel + draggable
  scrollbar), fits any screen size, "Need all / Greed all / Pass all" buttons,
  new rolls appear live.
- **"Loot won!" toast** and a sound when a new roll starts.
- **Clickable [Need] [Greed] [Pass] buttons in chat.** Players without the mod
  installed can vote too.
- **Better boss support:** loot from bosses with custom death logic (drops after a
  death animation, spawned straight into the world) is now captured automatically
  in most cases.
- **One lot per mob:** all of a mob's drops are gathered together, identical items
  merged, then rolled per stack, per item or as a whole lot (configurable).
- **Offline winners** receive their loot on next login.
- Tie-break rerolls are now announced in chat.

## ⚙️ Config
- Entity list now accepts tags (`#forge:bosses`) and whole mods (`modid:*`).
- New server options: item blacklist, minimum rarity, minimum players, vote mode,
  line-of-sight requirement, killer always included, disable Greed, roll range,
  what happens when nobody wants an item or the winner is offline, chat verbosity,
  chat vote buttons, /startvote permission level and cooldown, /roll radius.
- New client config (`lootroll-client.toml`): HUD position and offset, number of
  frames, sound, auto-open screen, win toast.
- Existing config files keep working.

## 🐛 Fixes
- Loot could be lost if the winner relogged or left before the vote ended.
- Loot in running votes was lost when the server stopped.
- Item duplication when the winner's inventory was almost full.
- Different client/server clocks could auto-pass votes or break the timer bar.
- "Active loot vote" alert could stay on screen forever.
- Players behind walls or pillars were excluded from votes.
- Players outside the vote could cast votes.
- Vote type in chat wasn't translated; item count in results wasn't shown.

## ⚠️ Notes
- Client and server must both run 1.1.0 (not compatible with 1.0.x).
- The Ice and Fire / Alex's Caves / Dungeon Now Loading integrations are 1.20.1-only;
  on 1.21.1 boss loot is handled by the generic capture.
