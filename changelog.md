#####Version 0.8-27
- re-added documentation on biodiesel (thanks Malte)
- changed the plantoil value for hemp seeds back to what it was in 1.7.10
- changed internal rendering of connectors to use vflip again rather than flipped textures (thanks Malte)
- added comparator support to the current transformer (thanks Malte)
- added a hacky workaround for WAILA making wires look bad (WAILA should fix this, but we'll manage) (thanks Malte)
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
- fixed hte crusher still hurting entities even when turned off (thanks Malte)
- fixed IE blocks not being chiselable (thanks Malte)
- fixed maneuver gear dupe bug (thanks Malte)
- fixed furnaces not lighting up when using external heater (thanks Malte)

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