|Classes|Changes required|
|---|---|
|client.*FontRender|Update, not done during the main update process since `FontRenderer` changed a lot and testing will be necessary during the update|
|common.asm.*|Re-add as an JavaScript coremod, figure out where to inject the hooks now|
|common.blocks.multiblocks.*|Update to use `TemplateMultiblock` (and create the relevant templates)|
|common.util.compat112.*|Update to 1.14 as the other mods are updated|
|common.blocks.wooden.GunpowderBarrelBlock|[Related forge issue](https://github.com/MinecraftForge/MinecraftForge/issues/5841)|
|common.crafting.RecipeBannerAdvanced|Banner creation changed completely in 1.14, figure out how to hook into the new system|