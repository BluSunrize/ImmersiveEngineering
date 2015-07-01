package blusunrize.immersiveengineering.common.util;

import java.util.UUID;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import com.mojang.authlib.GameProfile;

public class FakePlayerUtil
{
	private static GameProfile IE_PROFILE = new GameProfile(UUID.fromString("99562b85-bd1a-4ded-bb1a-c307bf0c0133"), "[ImmersiveEngineering]");
	private static FakePlayer fakePlayerInstance;
	
	public static FakePlayer getFakePlayer(WorldServer world)
    {
		if(fakePlayerInstance==null)
			fakePlayerInstance = FakePlayerFactory.get(world, IE_PROFILE);
		return fakePlayerInstance;
    }
    
}
