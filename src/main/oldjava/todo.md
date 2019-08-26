|Classes|Changes required|
|---|---|
|client.*FontRender|Update, not done during the main update process since `FontRenderer` changed a lot and testing will be necessary during the update|
|common.asm.*|Re-add as an JavaScript coremod, figure out where to inject the hooks now|
|common.blocks.multiblocks.*|Update to use `TemplateMultiblock` (and create the relevant templates)|
|common.util.compat112|Update to 1.14 as the other mods are updated|