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