#####Version 0.6.0
- added a house for the IE Villager. He should be a lot less rare now!
- added Electric Lanterns and Floodlights, which will consume power but prevent mobspawns!
- added a Coal Vein to the Excavator, which can contain Diamonds and Emeralds too
- added Wooden Barrels to serve as portable fluid storage. Wont take hot fluids!
- added Sheetmetal Tanks to store large amounts of fluid
- added Silos for large, single-item, storage
- added Phial Cartridges to shoot potions with!
- added particles and a molten metal animation to the Arc Furnace
- added Achievements!
- added HydrauliCraft support for Hemp
- added a recrafting recipe to turn treated wood slabs back into treated wood blocks
- added a bunch of canFill and canDrain checks because no mod developer but MineMaarten seems to respect the methods for them >=(
- added a statistic to track the distance travelled by Skyhook
- added EE3 compat back in. All ingots as well as certain materials and the bullets have an EMC value now
- added Thermal Expansion compat. Basically allowing Constantan to be made in the Smelter
- added Shaders. These will allow you to change the look of your revolvers (and other items in future). Think paletteswap
- added treated wood as a separate block, added update methods accordingly. Fixes solidity issues and useability for carpenters and others
- added Fluid Pipes! They transport fluids! (thanks @UnwrittenFun!)
- added Fluid Pumps! They collect fluids and improve a pipe's throughput! (thanks @UnwrittenFun!)
- added config option to drop orphaned connections (thanks @cobra)
- added Nether- and Dense Ores support to the Arc Furnace
- added a search function with spellchecking to the manual
- added a headshot functionality to the revolver
- added the Assembler, an IE autocrafting machine
- changed wire rendering to be ISRBH, improving performance
- changed a lot of internal handling of the Energy System:
	- Wire Transferrates are now per tick. WIres can only have a certain amount of power go through them in a single tick
	- Too much power will make the wires burn away
	- TODO: buff the transferrates
	- TODO: change loss calculations
	- added more safety to avoid CME (shoutout to @mindforger)
	- fixed priorities on indirect connections (thanks @malte0811)
	- a big "Thank You" to @theunkn0wn1, @mindforger, @malte0811, @cobra and everyone else helping with the rebalancing! Dunno what I'd do without you guys :D
- changed Breaker Switch, it has a new model and emits redstone
- changed mineral distribution of the Excavator. You may lose your old deposits because of this
- changed Multiblocks in the manual. They can now be dragged to rotate, as well as show their completed render
- changed some of the NEI handlers to support GUI redirection
- changed Graphite Electrodes to track damage via NBT data
- changed the Engineer's Manual, restructured some entries into new categories and added a bunch of new ones
- changed IE particles to run through a custom render dispatcher, similar to Botania
- changed the Materials required for a Refinery. IE Multiblocks are a little too expensive some times =P
- changed multiple machines to accept enough power based on their consumption config
- changed big portions of the code to use less getTileEntity() calls (thanks @cobra & @malte0811)
- changed static parts of the multiblocks to use ISRBH in favour of the laggy TESRs
- fixed SideOnly annotations, should fix that Railcraft tank thing among others
- fixed burn time of coal coke block in furnaces
- fixed blockupdates for wooden poles
- fixed MineTweaker integration for Squeezer and Fermenter (thanks @Yulife)
- fixed a NPE related to homing cartridges
- fixed a dupebug with Thaumcraft's Magic Mirrors
- fixed many things about the crusher. Desync, animation issues, incorrect consumption of energy (thanks @cobra)
- fixed inability to place metal decoration blocks while jumping (thanks @UnwrittenFun)
- fixed output bugs with Squeezer and Fermenter
- fixed Raytracing errors (thanks @cobra)
 -fixed broken wirecutter, one-way wires and wire-limiters not getting reset (thanks @malte0811) 
- fixed Coke Oven not stacking bottles to 16
- fixed 'simple' IE blocks not being moved by pistons
- fixed hemp growing further than intended by mods like ProjectE that do not respect the "canBonemeal" method (thanks @cobra)
- fixed Lightningrod getting guaranteed hits
- fixed wooden posts breaking arms that don't belong to it (thanks @cobra)
- fixed side solidity on the Diesel Generator (thanks @cobra)
- fixed energyloss exceeding 100% (thanks @cobra)
- fixed transparency and light opacity for certain blocks
- fixed Nether Ores in the crusher ignoring their config
- fixed incorrect uses of CoFH API and repackackign the wrong package-infos (thanks @cobra)
- fixed world data not getting loaded under certain conditions (thanks @cobra)
- fixed breaker switch updating at the wrong time
- updated zh_CN.lang
- added ja_JP.lang
- updated ru_RU.lang
- added fr_FR.lang


==============
##### 0.5 VERSIONS ######
==============
#####Version 0.5.4 - BUILT
- added Concrete and Concrete Tile
- fixed a Nullpointer Exception in the Arc Furnace, triggered when a recipe without slag is processed
- fixed Minetweaker integration for Squeezer and Fermenter
- fixed CokeOven and BlastFurnace dropping an extra block when broken
- removed client update calls when machines receive RF. This might have been a cause of lag, but also might have consequences for WAILA
- fixed a typo in an oredict name (thanks xbony2)
- fixed item insertion and extraction for Squeezer and Fermenter
- fixed item instert for coke oven
- added Silver Bullets
- added another saving hook to (hopefully) fix the issue of disappearing wires
- Oblivion


#####Version 0.5.3.2 - BUILT
- fixed raytracing on HV relays (thanks cobra)
- updated zh_CN.lang
- fixed dupebug with blueprints (thanks malte0811)
- fixed tooltips rendering below text (thanks UnwrittenFun)
- introduced mod-priority system, allowing users to configure which mod they'd like the ores the excavator spits out to be from
- fixed Excavator's chance system for good, hopefully
- added blacklist for dimension IDs, rather than restricting IE ores to the overworld
- added a config to force-enable the crushing of ores with the Engineer's Hammer
- added Minetweaker support for the Arc Furnace
- fixed the appearance update of the Arc Furnace, even when it's shut off by redstone
- moved coke blocks, coke oven bricks, blast furnace bricks and hempcrete to a separate block class to allow for multiparts and carpenters blocks
- Arc Furnace should respect the stacksize limit for outputs now
- fixed a rare NEI crash
- fixed DenseOres not registering for the crusher
- changed internal recipe handling of fermenter and squeezer
- made changes to the lightning rod, improving its chances of getting hit
- fixed crop growth display for WAILA
- fixed extraction of items from Arc Furnace
- fixed derpy crafting-navigation-arrows in manual
- added metal dusts smelting in the Arc Furnace


#####Version 0.5.2 - BUILT
- fixed boundingboxes on slabs
- fixed shift-clicking items in the arc furnace
- fixed Arc Furnace structure + collision
- fixed broken Blueprint crafting
- fixed Arc Furnace not updating its electrodes when shut off by redstone
- fixed Raytracing of connectors allowing for invalid connections
- fixed placement issues of connectors replacing blocks and hovering in the air
- added localization for IE villager
- attempted to fix blueprints spawning as dungeon loot
- disabled EE3 compat module till I can make it work right


#####Version 0.5.1 - BUILT
- removed OreDict tooltip and moved tooltips to clientside-only
- fixed degrading electrodes in unpowered Arc Furnace
- added additional bounding box blocks to Arc Furnace, will require the furnace to be rebuilt
- fixed Electrodes not rendering
- removed Arc Furnace from creative tab (users are a bit dense >_>)
- fixed slabs not placing against doubleslabs
- fixed Villager selling the wrong wire
- fixed steel not being craftable into blocks, nuggets and slabs


#####Version 0.5.0 - BUILT
- added Arc Furnace! Yes, it's a thing now!
- added metal slabs
- added Blueprint crafting. This will be used for bullets and other things in future
- added Homing and Wolfpack bullets
- added Engineer Villager to sell IE items
- added metal nuggets
- redid API stuff. Addons might break
- changed crusher API, allowing for multiple secondary outputs, will probably break AOBD
- changed OreDict handling in recipes. Should improve performance
- changed crusher to build most Ore->Dust & Ingot->Dust recipes dynamically
- added a world saving hook in the hopes of fixing that issue of wires vanishing on world reload
- moved Workbench back to TESR because of derpy lighting
- fixed critical bug with revolvers
- connectors only accept power from the correct side now
- the mousewheel can be used to changed pages in the manual
- changed poweroutput config for dynamos to a double, to allow for more precise modifiers
- fixed derpy in-hand rendering for fences
- added WAILA compat for hemp
- buffed electro upgrade for the revolver
- changed bullets to be craftable with blueprints and added config for more costly bullets
- changed Capacitors to no longer be ImmersiveConenctable. Dunno why I made them that in the first place...
- prevented Multiblocks and Connectors from being teleposed by BloddMagic
- updated Manual for new content added
- changed formatting in manual, addon devs beware!
- changed all textures to be power of 2, because old GPUs can't handle my fancyness
- fixed raytracing on LV connectors
- changed localization of High-Voltage Wire Coil to match its connector
- enabled the Diesel Generator to be mirrored by clicking the center of the generator-block side
- fixed BlastFurnace gui opening clientside
- added another NPE check to wire rendering
- fixed TCon interaction
- fixed BlastFurnace glitch of running out of fuel before finishing a stack of iron
- moved most recipe additions of IE to its own class
- fixed revolver render to finally show the internal parts of the electrodes
- allowed placing of ladders/torches/other things on certain parts of the multiblocks
- extended GregTech support to include most ores, also possibly fixed energy transport
- fixed revolver render to build correctly for flavour-linked render additions


==============
##### 0.4 VERSIONS ######
==============
#####Version 0.4.4 - BUILT
- fixed vertical wires not rendering
- dynamo can now be rotated with the hammer


#####Version 0.4.3 - BUILT
- important hotfix for TileRenders. They should work now
- fixed capacitors not syncing their energy storage
- fixed derpy boundingboxes on the arms


#####Version 0.4.2 - BUILT
- fixed rendering derp with backtools
- added config for energy output on the ThermoElectric Gen
- fixed coke oven to reset timer on input change
- added external access methods for the Sample Drill
- added null check to wire rendering
- still working on derpy skylines .-.
- fixed replacement ores in the excavator
- more changes to the API, allowing for more specific colouring of wires, textures for them and customizeable slack
- nerfed Kinetic Dynamos to only take power from one side now
- optimized connections to cache catenaries. Improves rendering performance
- added No Name
- ominous changelog is ominous.
- fixed arms on wooden posts to only connect where appropriate (bounding boxes \o/)
- added config option to disable hammer crushing
- allowed revolver recipe to be mirrored


#####Version 0.4.1 - BUILT
- updated zh_CN.lang
- fixed randomness of the excavator for negative seeds
- allowed customizeable renderdistance for windmills, watermills and a few others
- made wooden posts climbable. Temporary feature, might be removed again.
- fixed localization on voltmeter
- added potatoes to the list of items to be fermented
- started working on skycrates. Heavily WIP, be warned!


#####Version 0.4.0 - BUILT
- fixed conveyor sorter not accepting items
- players can no longer ride bullets, sorry =P
- players now ride the catenaries of the wires instead of straight lines
- added temporary render to the Skyhook...and cancelled it shortly afterwards
- moved fuel-consumption in the blastfurnace behing smelting
- apparently Charcoal isn't oredicted by default, it should now work in the blast furnace though
- fixed IC2 machines voiding power
- added damage-boost for skyhook when the payler is falling, this will be an upgrade
- Lanterns got a new render
- Lightningrod now doesn't work for spawned lightning anymore
- changed internal methods for the manual
- added a new functions to the API, allowing a check if power can pass through a connector
- added methods to the API to determine whether a wire transfers power
- added a breakerswitch to use those functions
- added more placing options to wallmounts
- added downwards-facing arms to wooden poles
- added comparator output to capacitors
- added zh_CN.lang
- added a list of required blocks to every multiblock
- attempted fix for the transfer of negative energy 
- attempted fix on the sound system...again
- changes to revolver crafting, even users of custom skins can now get the normal one
- stopped fluid producers from stacking unstackables
- fixed Industrial Hemp page linking to the wrong page
- refactored the API
- added manual support into the API, so addon devs can do custom entries&categories
- Fermenter&Squeezers no longer consume 9 of every stack but 9 in total
- fixed hammer recipes to no longer consume the hammer and priorizite IE dusts
- fixed ItemRouter voiding more stuff
- fixed excavator being unlimited for negative numbers


==============
##### 0.3 VERSIONS ######
==============
#####Version 0.3.3 - BUILT
- started working on skylines
- fixed NEI handlers for squeezer+fermenter
- removed a debug recipe I forgot about
- updated ru_RU.lang
- made NEI handler cycle though input-variations
- fixed the NBT reading for WireTypes
- fixed crashing sounds
- added another fluid check to fermenters and squeezers to avoid NPEs
- added replacement system to excavator, can replace Uranium with Yellorium when necessary
- added render-diameter to Wire-Types. This is an API update so it might break stuff
- added a "complete" multiblock display of the excavator to the book
- made links in the book more prominent
- added support for nether ores
- fixed connection-removal resulting in NPEs
- Sample Drill needs blocks below it now
- change recipe for mechanical components to fix a conflict


#####Version 0.3.2 - BUILT
- updated ru_RU.lang
- added shiftclicking to Workbench
- made drillheads repairable again
- buffed drillhead damage to match the material cost in comparison to pickaxes
- added shiftclicking to Refinery
- changes to formatting in Minetweaker integration
- fixed wire rendering
- possibly fixed mineral calculation


#####Version 0.3.1 - BUILT
- packed ice fixed for thermoelectric gen
- properly initialized WireTypes, connections shouldn't turn into copper anymore
- fixed derpy refinery output, it also prioritizes internal storage items before outputting now
- added a @SideOnly to the icon stuff in the drill, might fix Railcraft derps
- made the cokeoven sync when fluid is extracted
- fixed the broken lightning rod
- added some extra security in fluidoutputting on Fermenter and Squeezer


#####Version 0.3.0 - BUILT
- fixed the ridiculously fast CokeOven
- added ru_RU.lang
- fixed speedloaders not working
- added filled capacitors to creative+NEI
- fixed some broken crusher recipes
- fixed slot-restrictions of Fermenter+Squeezer
- fixed derpy conveyor placement
- added config for Revolver damage
- fixed a render derp with the workbench
- added Minetweaker support for Crusher,Squeezer,Fermenter,Refinery
- renamed WallMount stuff to Wallmount, because gradle is a derp
- rewrote the API to accomodate custom WireTypes
- possibly fixed the weird percentages on minerals 


==============
##### 0.2 VERSIONS ######
==============
#####Version 0.2.4 - BUILT
- fixed various spelling error in the .lang
- fixed HV Transformers fitting on wooden poles
- fixed Revolver GUI, revolver is now locked in slot
- made changes to the API, to allow for OpenComputers integration
- cleaned up code in multiple places (like that Json testing in the config >_>)
- added Minetweaker support for the Crusher. It's untested.
- fixed the back button in the manual
- fixed removeFuel Minetweaking on the Blast Furnace
- possibly fixed winmills and watermilsl derping on certain chunk positions
- also hopefully fixed lightningrods in that regard
- tentative fix for that concurrent exception on rendering
- added safety check on Bucket Wheel rendering


#####Version 0.2.3 - BUILT
- added Engineer's Workbench
- buffed FluidTank on the Drill
- nerfed Mineral veins
- added new Revolver models
- added Revolver upgrades
- added Internal Tank upgrade for Drill
- rebalanced Drill Upgrade recipes
- added manual entry for Revolver and Workbench


#####Version 0.2.2 - BUILT
- possible fix for fermenter+squeezer crash
- structural connectors rotate again
- fixed up the manual on the excavator a little
- added documentation to Structural Connectors


#####Version 0.2.1 - BUILT
- fixed mineral depletion on the excavator


#####Version 0.2.0 - BUILT
- fixed CokeOven texture
- added NEI Handler for the crusher
- fixed RedstoneOre giving Platinum dust
- Excavator and Bucket Wheel have been implemented
- Refinery has a recipe system and a prettier GUI
- Fermenter and Squeezer have recipes and an extra output slot
- Item Router should no longer delete items. Hopefully this fix didn't increase the lag
- Drills should no longer work well without fuel
- Mineral Deposits should deplete over time
- Added Core Sample Drill to determine minerals in chunks
- Added some OreDictioanry support to structures, because compat
- Changed Wire behaviour to be parallel rather than consecutive
- Added AquaTweaks compat


==============
##### 0.1 VERSIONS ######
==============
#####Version 0.1.16 - BUILT
- fixed cyclic exception in NBT read/write on wooden crates
- drills. 'nuff said.
- buffed the hempcrete recipe, only requires clay, not full blocks now
- wooden crates don't write unnecessary NBT on breaking, so empty crates can stack again
- Coke Oven has a new texture


#####Version 0.1.15 - BUILT
- fixed dependency on CCLib
- Refinery has a temporary gui
- connections no longer glow in the dark but have some other weird lighting issues
- dieselgen model doesn't reload when holding shift


#####Version 0.1.14 - BUILT
- crusher multiblock can be mirrored now. Also structure can be initiated from the opposing side
- changed rendering of wires, moved from TESR to RenderWorldLastEvent
- fixed a possible NPE on power transfer
- fixed CokeOven and BlastFurnace only allowing 63 items in the output
- revolver shenanigans everywhere
- moved all the connectors, wooden poles and the transformers from TESRs to ISRBHs
- most other models are also .obj now
- added the sorter
- Diesel Generator has a new model
- Refinery will no longer run off of water (whoops >_>)


#####Version 0.1.13 - BUILT
- revolvers now expel the right casings
- coke oven, fermenter and squeezer now check if they can use a filled fluid container before creating one. No more fluid voiding
- HV transformers no longer delete blocks on placement
- water wheels should stop stopping all the time
- changed windmills to check lines in the innermost loop
- added MFR support for hemp plants
- stopped the energy system from being derped


#####Version 0.1.12 - BUILT
- ores should now work with BC quarries


#####Version 0.1.11 - BUILT
- fixed a bug with table pages initializing with empty OreDict lists


#####Version 0.1.10 - BUILT
- added wooden and steel wall mounts
- improved squeezer and fermenter, no more while loops. Hopefully lag-preventing
- made watermills sync their rotation when stack against eachother
- tried to improve conveyor belts
- melons work in the fermenter to produces ethanol now
- fixed some localization derps
- buckets of fluid no longer stack, and bottles are limited to 16
- IE's fences no longer block chests from opening
- Structural Arms now invert if the aim is above half the block, not above two thirds
- IE multiblocks now show their energy stored in WAILA
- Crusher actually consumes power
- Diesel Generator has Redstone control now
- changed vector calculations on watermill. Should reduce lag
- reduced vein size from Bauxite (at least the default config for it). It's apparently quite abundant 
- add pickBlock support for capacitors, to make creative testing easier
- crusher particles no longer break all the things and also spawn according to currently processed item
- wires actually do lose energy now!
- added partial tick time to windmills, dieselgen's fan and crusher
- added table to the DieselGen entry, showing possible fuels
- renamed watermills to water wheels


#####Version 0.1.9 - BUILT
- added container items to creosote buckets+bottles


#####Version 0.1.8 - BUILT
- fixed sounds. Should not crash the server anymore and also loop fine
- fixed the rendering of insulating glass
- structural arms can be inverted
- fixed slots and items valid for Coke Oven and Blast Furnace
- more revolver fancyness
- made RF>EU conversion rate configureable
- improved watermill render. Hopefully less jittery.
- nerfed blast furnace fuel
- fixed cable lengths. Thanks Tahg =P
- added External Heater, a block that will consume RF to power adjacent vanilla furnaces
- added the lacking smelting recipe for electrum grit
- made various block sides solid, where necessary
- started work on wall mounts


#####Version 0.1.7 - BUILT
- fixed blending on the text on the manual
- fixed the crash caused by calculating loss
- added a colourblind config so people can still see what capacitors sides are configured too
- added secondary outputs for the crusher
- crusher destroys items again
- took partial tick time into consideration when rendering the windmill...at least I think I did?
- fixed the spawning of revolver bullets...maybe
- added a NPE check to connection rendering. As for how Myst managed to produce it: No idea.
- EDIT: might have found out how it happened. Hopefully fixed it
- added Railcraft's RockCrusher recipe as a fallback for the crusher
- removed the catenary warning log
- watermills should stack up to three now
- improved Blastfurnace and Cokeoven updating upon structure creation
- fixed clientside gui of the wooden crate
- added structural cables and structural connectors for decorational purposes
- did fancy things with the revolvers


#####Version 0.1.6 - BUILT
- added container items to fluid containers
- cokeoven will now properly smelt coal blocks into coke blocks
- the arrow to leave entries in the manual now resets the page number again
- cokeoven, squeezer and fermenter will now properly process till their tank is filled
- conveyors will move players and items properly again
- boosted the plantoil output on hemp seeds
- diesel generator sounds now stop when the generator is broken
- windmills now face the right way


#####Version 0.1.5 - BUILT
- NPE fix. Really should have had it in the previous


#####Version 0.1.4 - BUILT
- fixed the manual. Pls no crash anymore D:
- added documentation to the hemp


#####Version 0.1.3 - BUILT
- finished conveyor belts
- fixed various bugs


#####Version 0.1.2 - BUILT
- updated textures
- added documentation for the changed capacitor functionality
- fixed DieselGen, now accepts fluids correctly again and uses them
- changes to the code determining the crusher's structure. Possibly a bugfix?
- made worlgen configureable
- added a page+category hiding functionality to the manual Lib
- laid out basics for conveyor belts


#####Version 0.1.1 - BUILT
- introduction of the public changelog
- fixed placement of wooden slabs
- finished crusher, added IInventory support
- capacitors now update correctly and can have their opposite side changed
- diesel generator should now break into components correctly and had its bounding boxes fixed


#####Version 0.1.0 - BUILT
- initial beta release 