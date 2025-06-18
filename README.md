# danse (dance)
> player gestures/emotes for fabric servers using 1.21.4+ item models

There are 9 gestures at the moment;
- bits
- fall
- grow
- helicopter
- handstand
- wave
- zombie
- dab
- facepalm

More are in the works...!

Using vanilla item models!\
Compatible with Sodium, Iris, and everything else!

Clients don't have to install any mods, they can connect with a vanilla client!


# Commands:
```
/gesture bits
/gesture fall
/gesture grow
/gesture helicopter
/gesture handstand
/gesture wave
/gesture zombie
/gesture dab
/gesture facepalm
```

# Models

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

# How?

The mod uses the new 1.21.4 item models and custom_model_data to dynamically display the skins.

---

# Adding custom animations

Put your .bbmodel or .ajblueprint files into the `config/danse/` folder.
You get can templates [here](https://github.com/tomalbrc/danse/tree/main/src/main/resources/model/danse)

The name of the animation will be used in-game

# License

Versions before 2.0.0 are LPGL-3.0 licensed.
