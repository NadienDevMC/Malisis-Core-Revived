# MalisisCore Revived

A port of [MalisisCore](https://github.com/Ordinastie/MalisisCore) (originally by Ordinastie and PaleoCrafter, MIT license) to **Minecraft 1.21.1 / NeoForge 21.1.240**.

MalisisCore is the library used by the Malisis mods (see [MalisisDoors Revived](https://github.com/Modern-Mods/Malisis-Doors-Revived)).
The 1.12 version was a huge framework (custom renderer, GUI toolkit, ASM transformers, chunk collision...). Most of it is now covered
by vanilla and NeoForge, so this port keeps the parts the Malisis mods actually need, rewritten for the modern APIs:

| Package | Content |
| --- | --- |
| `nadiendev.malisiscorerevived.animation` | The transformation framework: `Translation`, `Rotation`, `Scale`, `AlphaTransform`, `ChainedTransformation`, `ParallelTransformation`. Transformations are applied to a `PoseStack` (through `Transformable`) in block entity renderers, with the same semantics as the original (offsets relative to the block center, reversible, delays, loops, sinusoidal movement). Times are in ticks and support partial ticks. |
| `nadiendev.malisiscorerevived.client.model` | A small Wavefront OBJ loader (`ObjModel`, `ObjModelCache`) with named groups, rendered with any block atlas sprite. |
| `nadiendev.malisiscorerevived.client.render` | Rendering helpers: render block models or boxes in block entity renderers, retexture quads, tints, UV helpers. |
| `nadiendev.malisiscorerevived.multiblock` | A generic multi-block system: an origin block implementing `IMultiBlockController` plus invisible `malisiscore:multiblock_part` blocks that delegate shapes, interactions, breaking, ladders, sounds and particles to it. |
| `nadiendev.malisiscorerevived.util` | `AABBUtils` (rotations, shapes), `DirectionUtils`, `Timer` (game time based), `BlockEntityUtils`, `BlockStateUtils`. |

## Building

Requirements: JDK 21.

```
./gradlew build
```

To use it from another project (MalisisDoors does it automatically when both repositories are checked out side by side):

```
./gradlew publish          # publishes to ./repo
./gradlew publishToMavenLocal
```

## License

MIT. MalisisCore was created by Ordinastie and PaleoCrafter; this port is maintained by Modern Mods.
