Fix Me
------

To Test
-------
* OpenComputers integration

Fixed
-----
* cannot get a text readout from barrels when holding a bucket
* Redstone relays do not appear to pick up weak redstone signals (relay on block, lever on side of block)
* Liquid Storage in general does not output the text readout while holding a fluid container
* Unlocalized/bugged "Fuel time" on coal coke; appears as "desc.ImmersiveEngineering.info.blastFuelTime fuelCoke

Cannot Reproduce
----------------
* Concrete debuff does not apply when concrete dries on you
  * seems to affect Survival players more than creative 

Known Issues (won't fix with the port)
------------
* Pipes can be disconnected
* blast furnace preheaters work. But animation broken, might leave that to blu?
  * Can't find any animation code for this at all.
* shift clicking with the balloon doesn't display the feedback text
  * Also happens on 1.10.2, Only shows for 2 seconds when item is selected.
* Hammering the side of a lamppost causes the new "arm"to render as an entirely new post
  
Untestable
----------
* IndustrialCraft 2 integration
* WAILA integration
* Tinkers Construct

Issues caused by Port
--------------
* Electrum Coil Blocks and High Voltage Coil Blocks show missing model
* Giving an item to an unconfigured router causes a crash
* Crash when placing a balloon
* Crash when using the engineer's workbench
* Accessing the Item Router GUI freezes the game
* Crash when selecting the revolver.
* Cloche is now no longer rendering at all
* Breaking a coke oven while it's in progress can constantly give you the input.
* Ensure all lang file issues are brought across
* Immersive Engineering does not load on Forge 2270+
* Unlocalized block name for the Breaker Switch
* voltmeter text is missing texture
* putting a railgun in a charging station crashes the client
* right clicking a workbench with a drill caused this crash
* There is some weird ass gl leakage going on when holding a drill. inventory gui doesn't display properly. water in world vanishes and a textureless bow can sometimes render floating below the player
  * Also applied to Chemical Thrower
* Covered conveyor belt does not render the scaffold section
* Placing a Core Sample Drill Crashes the client
* "Stopping" a multiblock demonstration in the manual also causes weird GL freakout
* HOP granite dust and ingot are both missing textures
* Trying to form the Bucket wheel crashes the game
* Trying to form the silo crashes the game
* Applying a plate to the press crashes
* trying to fill a jerry can in a bottling machine causes a crash
* Turntable does not appear to function at all
* texture on the bottle machine is bork as fuuuuu
* Arc Furnace will not form
* Game Still crashes when forming Bucket Wheel
* Game still crashes when forming the Silo
* Can't Shift click into the workbench
* Can't shift-click coal and/or empty bottles into coke oven
* Can't shift click items into the blast furnace
* Can't shift click items into the improved blast furnace
* Drills 3x3 not working
* Buckets don't work in refinery
* game crashes when connecting power to Arc Furnace