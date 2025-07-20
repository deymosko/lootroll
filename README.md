# 🎲 LootRoll

A Minecraft Forge mod (1.20.1) that adds a **Need / Greed / Pass** loot voting system inspired by classic MMORPGs like WoW or FFXIV.

---

## ✨ Features

- 🧍‍♂️ Triggered by mob deaths (configurable whitelist)
- 📦 Players vote for each dropped item separately
- ✋ Only nearby players (within 100 blocks) can vote
- ⏳ 30-second voting timer
- 🎲 Highest roller wins: Need > Greed > Pass
- 🗨️ Chat shows full roll result history
- 🖼️ GUI with buttons and progress bar
- ⚙️ Configurable entity whitelist
- 🛡️ Prevents dupe with loot suppression logic

---

## 🌸 Configuration

Edit the config file:

`config/lootroll-common.toml`

Example:

```toml
LOOT_ENTITIES = [
  "minecraft:zombie",
  "minecraft:skeleton",
  "minecraft:warden"
]
```

Only mobs listed here will trigger the loot voting system.

---

## 🧑‍💻 Player Command

Manually start a vote with:

```bash
/lootroll [amount]
```

- `amount` *(optional)* — number of items from main hand to include in the vote  
- If omitted, the whole stack will be used  
- If nothing is held, nothing will happen

---

## 🖼️ Screenshots

### 🔔 Vote HUD Overlay
Shows while an active vote is in progress:

![HUD](screenshots/hud.png)

---

### 🗳️ Loot Voting UI
Each item in a vote is shown with buttons:
![Vote Screen](screenshots/screen.png)
---

### 💬 Chat Results
![Chat Result](screenshots/chat_result.png)
---
## 📦 Installation

1. Download and install [Forge 1.20.1](https://files.minecraftforge.net/net/minecraftforge/forge/)
2. Place the mod `.jar` file into your `mods/` folder
3. Launch the game

---

## 🧠 How it works

- When an entity dies:
  - If it's on the whitelist → **its loot is suppressed**
  - Items are rolled out via the voting screen
  - All nearby players can vote (Need, Greed, Pass)
- After 30 seconds (or once all players vote):
  - The item is assigned to the winner and dropped

---

## 💬 Localization

Supports custom language files.  
PRs for translations are welcome!

---

## ⚠️ License

MIT License – free to use, modify and distribute.

---

## 🛠️ Todo & Ideas

- [x] GUI with multiple item support
- [ ] Sound effects for votes
- [ ] Server config sync
- [ ] Configurable voting duration
