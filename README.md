# CommonlyUsed
A Minecraft server plugin containing commands like /spawn /home /back and way more

## Features
### Commands
 - /back - returns to last known location (on death, on teleport last location is saved)
 - /craft - opens a crafting bench
 - /enderchest - opens your enderchest
 - /sethome, /home, /delhome - saves, teleports and deletes your home location
 - /inventory [player] - opens the current inventory of another (online) player
 - /slimechunk - if the player is in a slime chunk you will receive a message saying that, otherwise it will show you the coordinates of the closest slime chunk
 - /setspawn, /setspawn [player]/[x y z], spawn - sets and teleports to the spawn
 - /setwarp [warpName], /warp [warpName], /delwarp [warpName], /warplist - saves, teleports, deletes and shows a list of warp locations.
 - /heal - sets health and food (saturation) to full
 - /fly - enables flying (even in survival mode)
 - /spectator - enables spectator mode
 - /speed [<walk|fly>/reset] <value> - sets your walk or fly speed. with reset you can reset these values to default.
 - /nickname [nickname] - sets a nickname for you that all other players can see (colors are supported). Can be reset when using /nickname without arguments.
 - /realname [player] - gives the real in-game name of a player using a nickname
 - /tpa [player] - sends a teleport request to a player
 - /tpaccept - accepts a teleport request from a player
 - /tpdeny - denies a teleport request from a player
 - /tpcancel - cancels your teleport request to a player
