package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.interfaces;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

public interface MBOverlayText<State extends IMultiblockState> extends IMultiblockLogic<State>
{
	@Nullable
	List<Component> getOverlayText(State state, Player player, boolean hammer);
}
