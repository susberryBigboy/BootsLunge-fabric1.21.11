# Boots Lunge

> **Fly freely through the air with the Boots Lunge enchantment!**

**This mod adds a unique enchantment exclusive to boots that enables multi-directional lunges and consecutive jumps.**

---

## 📦 Installation & Requirements

### Requirements
- **Client Side:** Cloth Config API (Required)
- **Client Side (Optional):** Mod Menu (Useful for accessing the config screen directly from the mod list)
- **Server Side:** Cloth Config API is NOT required on the server side.

### Multiplayer
- For multiplayer, this mod must be installed on both the server and client to function properly.

---

## 🎮 Keybinds (Default)

- **Open Config Screen**: `U`
- **Boots Lunge**: `R`
- **Directional Jump**: `WASD` + `Space` (In Air)
- **Quick Directional Jump**: `Ctrl` + `WASD` + `Space` (On Ground)
- **Emergency Brake**: `Shift` + `Space` (While Falling)
- **Auto Emergency Brake**: Hold `Shift` + `Space` (While Falling)

---

## ✨ How to Obtain

**The Boots Lunge enchantment can be obtained through standard gameplay in the following ways:**

- **Enchanting Table:** Can be applied directly to boots using an Enchanting Table.
- **Loot Chests:** Found as enchanted books or pre-enchanted boots in generated structure chests.
- **Trading:** Can be obtained through trading with villagers.

---

## ⚡ Basic Rules

- **Usage Limit:** The maximum consecutive uses depend on the enchantment level (`Level + 1` uses).
- **Count Reset:** Landing on the ground completely resets your usage count.
- **Environmental Resistance:** Propulsion force is reduced in water, lava, and powder snow.
- **No Resource Cost:** Does **not** consume item durability or food/saturation levels.

---

## 🚀 Lunge Modes & Features

### 1. Boots Lunge
- **Trigger:** Press the **Lunge Key** (`R` by default).
- **Behavior:** Lunges directly toward the direction your player is currently looking, regardless of your state or ground contact.

---

### 2. Directional Jump
- **Trigger:** Press the **Jump Key** while holding movement keys in midair.
- **Behavior:** Lunges in the direction of your movement keys (`W`, `A`, `S`, `D`) without changing your camera orientation. If no movement keys are pressed, you will lunge vertically upward.
- **Note:** Since this only triggers while off the ground, press the Jump key twice from the ground (e.g., hold `S` and double-tap `Space` to quickly leap backward).
- ⛔ **Restriction:** Cannot be used while equipping an Elytra (regardless of whether you are actively gliding or not).

---

### 3. Quick Directional Jump
- **Trigger:** Press `Ctrl` + **Jump Key** (`Space`).
- **Behavior:** Works like Directional Jump, but can be activated **instantly from the ground** without needing to be midair first.
- ⛔ **Restriction:** Cannot be used while airborne if an Elytra is equipped.

---

### 4. Emergency Brake
- **Trigger:** Press `Shift` + **Jump Key** (`Space`).
- **Behavior:** When falling from high altitudes, standard lunge propulsion might not fully counter your downward momentum. The Emergency Brake cancels your vertical velocity, applying a hard brake in midair.
- ⛔ **Restriction:** Cannot be used while equipping an Elytra (regardless of whether you are actively gliding or not).

---

### 5. Auto Emergency Brake
- **Trigger:** Press and hold `Shift` + **Jump Key** (`Space`).
- **Behavior:** If you hold the keys while falling, it automatically triggers right before impact to protect you from fall damage. Enabled by default.
- ⛔ **Restriction:** Cannot be used while equipping an Elytra (regardless of whether you are actively gliding or not).

---

## ⚙️ Configuration

### Client Configuration
- Open the in-game config screen using the designated key (`U` by default).

### Server Configuration
1. Edit and save the server configuration file (`config_server.json`) located in your config directory.
2. Apply changes in-game by restarting the server or executing the command: `/bl reload`
