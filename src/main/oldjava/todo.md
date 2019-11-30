|Classes|Changes required|
|---|---|
|common.asm.*|Re-add as an JavaScript coremod, figure out where to inject the hooks now|
|common.blocks.multiblocks.*|Update to use `TemplateMultiblock` (and create the relevant templates)|
|common.util.compat112.*|Update to 1.14 as the other mods are updated|
|common.crafting.RecipeBannerAdvanced|Banner creation changed completely in 1.14, figure out how to hook into the new system|
|common.block.BlockIEFluid*|This needs to be implemented in the fluids now, as fluid blocks appear to be gone|
|common.util.IEVillagerHandler|Update, maybe incorporate some of the new vanilla features/concepts?|
|common.util.VillageEngineersHouse|I think this uses structures/templates now?|
