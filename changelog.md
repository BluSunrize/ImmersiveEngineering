#####Version 0.10-52 - BUILT
- added the ability for Dropping Conveyors to drop through trapdoors
- added a new shader
- fixed Nullpointer for Connectors accepting FUs

#####Version 0.10-51 - BUILT
- re-added Festive Crates! Happy Holidays!
- re-added Botania compat for shaders, Relic rarity lootbags + shaders
- added the ability for Strip Curtains to emit redstone when an entity passes through
- added compatability for Forge Energy / Forge Units. End of an era, I guess.
- fixed a nullpointer on JumpCushions
- fixed Splitting and Dropping Conveyor not inserting into inventories
- fixed crashes in the manual due to incomplete tesselation
- fixed load-cycle with TCon
- fixed HarvestCraft recipes usign water/milk not working in the assembler
- fixed Furnace Heater constantly updating the block when insufficiently powered
- IE now has a Maven (thanks SkySom)

#####Version 0.10-50 - BUILT
- added versioncheck for TConstruct to prevent crashes with ranged weapons
- added Icons for all IE machines to JEI and fixed recipe displays
- fixed furnace heater constantly updating chunks
- improved UV maps on the revolver, added a new shader

#####Version 0.10-49 - BUILT
- rewrote the shader system
    - improved texture performance
    - changed API integration
    - added listing in the manual, including re-ordering
    - removed the recipe to turn shaders back into bags
- added the ability to rename wooden crates
- added IRotationAcceptor interface for dynamos and the things that drive them (thanks Malte)
- re-added the config for Excavator fail chance (thanks Malte)
- re-added the ability to dye balloons (thanks Malte)
- re-added the ability to add shaders to balloons
- changed the drill's hardness checks to easier dig ores (thanks Malte)
- fixed duped fluidcontainers on treated wood recipes
- fixed incompat issue with new versions of TConstruct
- fixed CraftTweaker compat (thanks mezz)
- fixed Crusher recipe for sand requiring grass rather than gravel (thanks MalkContent)
- fixed NPE on inserting items
- fixed Wirecutters consuming durability and not working properly on transformers
- fixed various client issues (thanks Malte)
- fixed Structural Connectors and Redstone Connectors not displaying properly (thanks Malte)
- fixed Conveyors throwing NPEs on Servers (thanks Malte)
- fixed dupebug with Charging Station
- fixed a few missing sounds
- fixed a concurrent modification in Tesla Coil sounds (thanks Malte)

#####Version 0.10-48 - BUILT
- added CTM support to Engineering Blocks and Scaffolds when Chisel is installed (in cooperation with Drullkus)
- changed Breakers to be rotatable (thanks Malte)
- changed Conveyor Belts showing walls when having their sides towards a metal press or assembler
- fixed missing comments in the config file
- fixed breaker switches crashing on servers (thanks Malte)
- fixed IE models causing lag on chunk updates (thanks Malte)
- fixed Breaker Switches not outputting redstone signals (thanks Malte)
- fixed Redstone Breakers ignoring their inversion (thanks Malte)
- fixed Ear Defenders being broken (thanks Malte)
- fixed Squeezers and Fermenters not outputting Items automatically
- fixed Silo not auto-outputting when supplied with a redstone signal
- fixed Railgun not shooting alu, iron and steel sticks from other mods
- fixed Manual on Railguns crashing without Railcraft installed (thanks Malte)

#####Version 0.10-47 - BUILT
- moved all clientside references from the config

#####Version 0.10-46 - BUILT
- fixed serverside crash due to abstract proxy
- fixed mcmod.info file

#####Version 0.10-45 - BUILT
- change over to Forge's new config system:
    - DELETE YOUR IE CONFIG
    - Or don't, but you'll get a load of duplicate and unnecessary options.
    - If an old config breaks anything, you were warned.
- added WAILA support for Tesla Coils
- added The One Probe Compat for machines and energy storage
- added a config option to disable villagers and their houses
- added a config option to disable OreDict names in Advanced Tooltips
- re-added config option for Railgun damage
- re-added support for Railcrafts rails and rebar to be used as projectiles
- re-added shader support for Railcraft carts
- re-added the placement options for Redstone Breakers (thanks Malte)
- changed blocks with configurable sides (capacitors, barrels) to use unlisted properties and lazy loading. Reduces IE's model count at startup by >75%
- change Refinery model to have a big power input like Fermenter and Squeezer
- fixed Refinery not properly accepting fluids
- fixed crash when crafting cetain items (thanks Malte)
- fixed Conveyors dropping invalid items (thanks Malte)
- fixed Pumps not working due to miscalculated XORs (thanks Malte)
- fixed Watermills having too small models
- fixed wonky wire rendering (thanks Malte)
- fixed material list for the Lightning Rod
- fixed Faraday suit not blocking Tesla damage
- fixed Floodlight having broken rendering and expanded computer support (thanks Malte)
- fixed Buckshot projectiles not applying full damage
- fixed Scaffolding not rendering properly on the inside
- fixed breakerswitch not emitting redstone
- fixed force chunkloading through redstone connectors
- fixed Toolbox voiding items
- fixed Pipes resetting connection configs (thanks Malte)
- fixed Silo voiding items and not having Comparator output
- fixed Pipes not dropping their covers
- fixed placement logic for multiblock placements (posts, transformers, drill, teslacoil)
- fixed CraftTweaker integration for the Refinery
- fixed stair rendering
- fixed explosion resistance for leaded concrete stairs and slabs
- fixed Crusher overloading its process queue
- fixed multiple minor issues

#####Version 0.10-44 - BUILT
- fixed clientside exception

#####Version 0.10-43 - BUILT
- added version dependency for TCon to prevent further reports of that
- added Banners! They look awesome! Shoutouts to Tris for making all the textures!
- added support for TConstructs resourcepack based materials
- added Strip Curtains. Purely decorative, dyeable, pretty nice for factories!
- re-added the ability to climb scaffolding on pipes
- re-added the Lightning Rod!
- expanded the "allow" and "interdict" functions on the Hammer in regards to multiblocks
- expanded the API on wires for an addon
- changed manualpages for Multiblocks to be better at everything (thanks boni)
- greatly improved renderperformance on waterwheels and windmills (thanks Malte)
- fixed wooden barrels not being toggleable
- fixed wooden barrels acceptign invalid fluids
- fixed side-solidity on pumps
- fixed changelog display in the Engineer's Manual
- fixed jerrycans not getting filled properly and beign lost in crafting
- fixed broken villager trades
- fixed glitches in the toolbox (hopefully)
- fixed <ERROR> overlays when WAWLA is installed (thanks Malte)
- fixed WAILA display for EnergyProviders
- fixed crash with Phial Cartridges
- fixed Homing Cartridges not doing damage
- fixed CraftTweaker method name
- fixed Balloons not emitting light
- fixed crash during Arc Furnace recycling calculations (thanks Malte)
- fixed Bucket Wheel continuing to rotate when the Excavator is broken and desyncing (thanks Malte)
- fixed LootEntry manipulation (3rd time's the charm?) (thanks Malte)
- fixed crash with Railcraft (thanks Malte)

#####Version 0.10-42 - BUILT
- fixed NPE when throwign items into the crusher
- attempted fix for render crashes with TESRs and their blockstates

#####Version 0.10-41 - BUILT
- added the function to dye conveyor belts!
- added the splitting conveyor belt, it alternates left and right!
- re-added the ability to pull recipes from JEI into the assembler
- re-added charge counter to railgun
- re-added some missing achievements
- re-added the ability to cover pipes in scaffold. IMC messages an define additional whitelisted blocks
- re-added Crafttweaker/Minetweaker integration
- updated TCon compat to latest TCon
- removed Fluid Bottles. People can shut up about those now >_>
- fixed Assembler not crafting and occasionally crashing
- fixed Arc Furnace rendering, output rates and dupebugs
- fixed slab rendering
- fixed wooden crates not updating comparators
- fixed floodlight being terrible (thanks Malte)
- fixed lots of bugs with wire models (thanks Malte)
- fixed crashes with projectiles
- fixed SkyHook being derpy
- fixed Transformers not attaching on posts
- fixed IE blocks not having placement sounds
- fixed selection and colision bounds on multiple blocks
- fixed OC comapt on the crusher being borked (thanks Malte)
- fixed lootentry access error (thanks Malte)
- fixed wires sometimes not rendering on servers
- fixed a manual derp (thanks Malte)

#####Version 0.10-40 - BUILT
- fixed machines crashing
- fixed sheetmetal tank not outputting

#####Version 0.10-39 - BUILT
- added Bloodmagic compat:
    - demon will bullets. 'nuff said.
- re-added Chisel Compat. Won't work till Chisel actually brigns IMC integration back in
- re-added Botania compat:
    - conveyor belts are immune to magnets
    - terrasteel bullets are back
    - armed the potatoes
- re-added Wolfpack Cartridges!
- changed Water- and Windmills to use normal TESR again after all
- changed Lanterns to be rotateable with the hammer
- changed a load of Assembler functionality. Fixed fluid crafting and container items
- changed multiblocks in the manual to indicate whether the components are in your inventory
- changed arcfurnace to output slag faster
- fixed lighting on wires and connectors (thanks Malte)
- fixed wire rendering accross chunks; they split into two renders to at least give indication now (thanks Malte)
- fixed recipes for Faraday Armor
- fixed incorrect checks for empty lists when creating ingredient stacks
- fixed Universal Bucket being initialized too late
- fixed crashes relating to missign Railgun ammo
- fixed Wooden Posts not extending arms
- fixed derpy looking railgun and missing sounds on the Revolver
- fixed windmills not recognizing walls properly
- fixed JEI rendering for Metal Press and Crusher
- fixed multiple capability issues on IE tiles
- fixed conveyors shooting items out to the sides when inserted into
- fixed floodlights crashing on placement
- fixed balloons not being consumed when placed in the air and suffocating players
- fixed conveyors from the creative menu being unplaceable
- fixed dieselgen crashing for invalid fuel
- fixed rotations on the bucket wheel
- fixed broken renderign on the ear defenders
- fixed drill and chemthrower losing fluid when modifying
- fixed shiftclicking bullets into the revolver
- fixed dieselgen and refinery not accepting fluids when mirrored
- fixed hemp being plantable on non-farmland
- fixed arcfurnace consuming too many additives
- various other small fixes!

#####Version 0.10-38 - BUILT
- added the new IConveyorBelt system
    - fully dynamic conveyor system, with custom models and all
    - addon mods can easily add their own!
    - features magnet prevention. Works on EIO, in future BM and Reliquary (?)
- added Crusher recipe for coke blocks
- re-added EIO compat, mostly Arc Recipes
- fixed Assembler not assembling (thanks Alex)
- fixed Chemthrower Projectiles making arrow noises
- possible fix for the black rendering wires
- fixed (finally, hopefully) the desync caused by forming Coke Oven and Blast Furnace

#####Version 0.10-37 - BUILT
- added the new IBullet system!
    - bullets are no longer based on metadata, but use NBT tags to differentiate types, their abilities are handled externally
    - updated manual accordingly
- added recipe for leaded concrete
- changed Conveyors to have a better placement system
- fixed crashes when shiftclicking in revolver gui
- fixed slots not assignign properly in revolver gui
- fixed a bunch of manual things

#####Version 0.10-36 - BUILT
- added Hemp Seeds to Forestry's bags
- changed Windmill and Improved Windmill to have models with 90% less polys. PERFORMANCE!
- removed unnecesary debug outprints
- fixed NPE in manual
- fixed issues on the manual's mineral pages
- fixed models for coresamples

#####Version 0.10-35 - BUILT
- re-added Open Computers compat
- cahnged waterwheel and windmills to use FastTESR
- fixed NPE in ShapedIngredientRecipe
- fixed Treated Wood display in the TCon Book

#####Version 0.10-34 - BUILT
- added TCon alloy recipes
- fixed crash in earmuff recipe, using a client access method
- fixed arc alloy recipes having too little output
- prepping for FastTESR hopefully soon!

#####Version 0.10-33 - BUILT
- fixed crashes when break smart models, parsed texture into baked builder
- fixed ItemRenders for multiblocks
- fixed rendering of Wind- and Watermills
- fixed entrylsits in the manual being short
- fixed the forming of multiblocks scrambling the player's inventory
- fixed treated wood recipe consuming buckets (Forge Bug, really...)
- fixed machines crashign when fillign buckets internally

#####Version 0.10-31 - BUILT
- added Redstone Wire connector
- cleaned up recipes and manual a little
- fixed wire rendering
- fixed crash with cart shaders

#####Version 0.10-30 - BUILT
- UPDATE TO 1.10.2

#####Version 0.9-29 - BUILT
- UPDATE TO 1.9.4
- mostly port, movign stuff to new fluid systems, capabilities, etc
- moved 3D Maneuver gear. It didn't fit the style of hte mod and was incredibly glitchy

#####Version 0.8-28 - BUILT
- re-added the Floodlight back in
- re-added the transformers that can attach to posts (thanks Malte)
- re-added the ability for the pump to not create cobblestone and improved the pump (thanks Malte)
- added the Faraday Suit, an armor to survive tesla coils and allow for lightshows! (code by Malte, art by Mr. Hazard)
- added sound and nicer animations to the metal press
- added new textures for the Capacitors! Celebrating IE's one year of public release! :D
- changed recipes for WireCoil. They now take wire which is made out of plate with shears/wirecutters or with the metal press
- changed wirecutters to take durability damage when removing wires from connectors
- added fluorescent tubes. They glow when near a tesla coil! (thanks Malte)
- added a "Bad Eyesight" config to make the manual render in darker, bold font. Can mess up formatting
- fixed a crash and dupe bug with the toolbox (thanks Malte)
- fixed weird render issues and crashes with multiblocks (thanks Malte)
- fixed silo voiding items (thanks Malte)
- fixed a crash when exploding bullets hit reactive creatures like endermen (thanks Malte)
- fixed a crash when pumping fluids out of sheetmetal tanks (thanks Malte)
- fixed rotation bug on the bucketwheel (thanks Malte)
- fixed a bug where houses try to spawn the nonexistent villager
- fixed most IE blocks letting through light when they shouldn't (thanks Malte)

#####Version 0.8-27 - BUILT
- re-added documentation on biodiesel (thanks Malte)
- re-added the Assembler! For all your autocrafting needs!
- changed the plantoil value for hemp seeds back to what it was in 1.7.10
- changed internal rendering of connectors to use vflip again rather than flipped textures (thanks Malte)
- changed conveyor belts to halt their animation when red
- added comparator support to the current transformer (thanks Malte)
- added a hacky workaround for WAILA making wires look bad (WAILA should fix this, but we'll manage) (thanks Malte)
- added a manual page for Uranium Ore
- added a proper sound to the revolver firing
- added the Tesla Coil! It will shock you!
- added the ability to allow Tinkers Tools to the toolbox, as well as a better API to allow adding them (thanks Malte)
- fixed sheetmetal tanks showing the wrong structure in the manual
- fixed recipe for leaded concrete stairs
- fixed gunpowder barrels exploding when punched
- fixed pipes not visually connecting to refinery
- fixed the arc furnace only processing one item at a time (thanks Malte)
- fixed the arc furnace voiding items if the first output slot is full (thanks Malte)
- fixed TESR render crash (I still totally blame Vanilla/Forge for that) (thanks Malte)
- fixed invisible molten metals in Tinkers compat (thanks Malte)
- fixed pipes not transferring properly (thanks Malte)
- fixed invisible wires on balloons (thanks Malte)
- fixed balloons not being placeable in mid air (thanks Malte)
- fixed bottles not getting filled in squeezer/fermenter (thanks Malte)
- fixed Skyhook pulling people into walls (thanks Malte)
- fixed Dropping Conveyors not dropping (thanks Malte)
- fixed entities getting stuck on uphill conveyors (thanks Malte)
- fixed scaffolding not beign climbable (thanks Malte)
- fixed the crusher still hurting entities even when turned off (thanks Malte)
- fixed IE blocks not being chiselable (thanks Malte)
- fixed maneuver gear dupe bug (thanks Malte)
- fixed furnaces not lighting up when using external heater (thanks Malte)
- fixed scaffolding not being placeable while jumping (thanks Malte)
- fixed preheaters not consuming power correctly and not having a manual entry
- fixed crusher crushing entities on collision


#####Version 0.8-26 - BUILT
- IE REQUIRES JAVA 8 NOW!
- added a load of Tinkers Construct compat:
	- Treated Wood tool material. Similar to normal wood, but no splinters
	- Constantan tool material. Additional weapon effects based on biome temperature
	- Slime Fluids works in the chemthrower again
	- fixed smeltery alloy recipe for Constantan
- re-added InvTweaks compat to the Wooden Crates
- re-added Railgun and Chemthrower documentation to the manual
- buffed the default damage of the Railgun
- changed silver bullets to do extra damage to undead (Witchery isn't updated, so they'd be useless otherwise)
- fixed Multiblocks looking derpy when formed (thanks Malte)
- fixed connection offsets on transformers (thanks Malte)
- improved connections on transformers (thanks Malte)
- fixed connections on pipes looking weird and broken (thanks Malte)
 

#####Version 0.8-25 - BUILT
- wires can transfer across unloaded chunks now! :D (thanks Malte)
- added reinforced (creeper-proof) storage crates
- changed voltmeter to allow measuring between two non-output points (thanks Malte)
- fixed an NPE in rendering
- fixed fluid pipes not rendering properly
- fixed invalidly added recipe to create dusts where no ignots exist
- fixed missing coke dust recipe
- fixed Arc Furnace not having solid sides
- fixed NPE in Improve Blast Furnace's capabilities
- fixed Crash with the Mining Drill (thanks Malte)
- fixed watermill outputting less than it should because of broken math (thanks Malte)
- fixed extremely high losses on wires (thanks Malte)
- fixed insulating glass being opaque (thanks Malte)
- ported miscellaneous other fixes from 1.7 (thanks Malte)
- added zh_CN localization (thanks to 3TUSK, Amamiya-Nagisa, bakaxyf, CannonFotter, crafteverywhere, IamAchang, Joccob, LYDfalse, UUUii)


#####Version 0.8-24 - BUILT
- massive shoutout to Malte for figuring out some proper wire rendering! :D
- added compat for DenseOres (crushing/smelting) 
- fixed lanterns not emitting light
- fixed silver bullets causing a crash
- fixed gunpowder barrels crashing because of a duplicate datamarker
- fixed gunpowder barrels dropping from explosions
- fixed CokeOvens not interacting with hoppers
- fixed wires not outputting to RF-only receivers (thanks Malte)
- fixed connections on HV relays (thanks Malte)
- fixed a crash caused by hammering the top/bottom of a wooden post
- fixed the stackoverflow in sheetmetal tanks
- fixed the themoelectric gen crashing (thanks Malte)
- fixed the waterwheels breaking more other wheels (thanks Malte)
- fixed textures on stone slabs (thanks Malte)


#####Version 0.8-23 - BUILT
I lost all my changelog before this point. Whoops.
basically: Initial git push with a lot of the machines and tools implemented. But not done yet.