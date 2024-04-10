# danse (dance)
> player gestures/emotes for fabric/quilt servers using core shaders

There are 7 gestures at the moment;
- bits
- fall
- grow
- helicopter
- handstand
- wave
- zombie

More are in the works..!

Using vanilla core shaders!\
Compatible with Sodium (but will break with iris/embeddium and similar mods that allow for custom shaders)

Clients don't have to install any mods, they can connect with a vanilla client!

---

### Commands:
```
/gesture bits
/gesture fall
/gesture grow
/gesture helicopter
/gesture handstand
/gesture wave
/gesture zombie
```

Persistent player models can be spawned like this:
```
/summon danse:player_model ~ ~ ~ {Player:Steve,Animation:wave}
```

Animation can be player by modifying the "Animation" NBT string:
```
/data modify entity @e[...] Animation set value wave
```

Changing the skin:
```
/data modify entity @e[...] Player set value Alex
```

---

[Checkout the discord](https://discord.gg/9X6w2kfy89) for more info

---

This project uses a heavily modified version of the shader and blockbench model from [bradleyq's stable_player_display](https://github.com/bradleyq/stable_player_display)

The benefits of doing it as a mod vs. an animated-java datapack is performance
