package net.gobbob.mobends.animation.layer;

import java.util.Arrays;
import java.util.Collection;

import net.gobbob.mobends.data.EntityData;

public abstract class AnimationLayer<DataType extends EntityData>
{
	/**
	 * Returns the actions currently being performed
	 * by the entityData. Used by BendsPacks.
	 * 
	 * @param entityData Provides some context for the layer to know
	 * what actions to return.
	 */
	public abstract String[] getActions(DataType entityData);
	
	/**
	 * Called by a Controller to perform a continuous
	 * animation.
	 * 
	 * @param entityData Provides some context for the layer to know
	 * how to perform it's action, and allows the animation to manipulate
	 * the pose of the target.
	 */
	public abstract void perform(DataType entityData);
	
	/**
	 * Adds actions to the specified list.
	 * 
	 * @param entityData  entityData Provides some context for the layer to know
	 * what actions to return.
	 * @param list The list to add actions to.
	 */
	public void addActionsToList(DataType entityData, Collection<String> list) {
		String[] actions = getActions(entityData);
		if (actions != null)
			list.addAll(Arrays.asList(actions));
	}
	
	/**
	 * This both performs the animation and adds actions to the list.
	 * 
	 * @param entityData The same as in @see perform.
	 * @param list The same as in @see addActionsToList.
	 */
	public void perform(DataType entityData, Collection<String> list) {
		this.perform(entityData);
		this.addActionsToList(entityData, list);
	}
}
