# EasyTechnology

[English](README.md) | [简体中文](README.zh-CN.md)

EasyTechnology is a small addon for GregTech: New Horizons that adds convenience items and machines, primarily for early- and mid-game automation.

## Features

### Entangled Card

The Entangled Card records a dimension and block position.

- Right-click a block to record that block's coordinates.
- Right-click the air to record the player's current coordinates.
- Insert the recorded card into an Entangled Miner to mine ores around that remote position, including positions in another dimension.
- A recorded card displays the enchantment glint.

### Entangled Miners

Entangled Miners extract real ore blocks around the position stored on an Entangled Card. Their work area can be adjusted with a screwdriver, and mining can be disabled with a soft mallet.

| Miner | Power source | Maximum work area |
| --- | --- | --- |
| Primitive Entangled Miner | Furnace fuel | 17 x 17 |
| Bronze Entangled Miner | Steam | 17 x 17 |
| LV Entangled Miner | EU | 17 x 17 |
| MV Entangled Miner | EU | 33 x 33 |
| HV Entangled Miner | EU | 49 x 49 |

The LV, MV, and HV work areas match the standard GregTech miners of the same voltage tier.

### Void Oil Location Card

Right-click the air while standing in a target chunk to record its dimension, chunk coordinates, underground fluid type, and remaining virtual oil amount.

Place the recorded card in an Oil Drilling Rig controller slot to make the rig extract oil from that chunk, including a chunk in another dimension. A recorded card displays the enchantment glint.

### Portable Crafting Station

The Portable Crafting Station opens a Tinkers' Construct crafting station without placing a block.

- Right-click the item to open its interface.
- A configurable hotkey can open it while it is anywhere in the player's inventory.
- It can be used for normal crafting and Tinkers' Construct tool repair or modification.

### Healing Ring

The Healing Ring is worn in a Baubles ring slot. While equipped, it restores a small amount of hunger and saturation every two seconds.

The recipe is available when Extra Utilities is installed because it uses the Healing Axe as an ingredient.

### Pipe-Free Virtual Resource Drilling

GregTech multiblock drilling rigs that collect virtual resources, such as underground oil and void resources, operate without mining pipes.

## License

See [LICENSE](LICENSE).
