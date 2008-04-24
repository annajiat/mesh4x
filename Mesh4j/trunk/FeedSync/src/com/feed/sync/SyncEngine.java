package com.feed.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.feed.sync.behavior.MergeBehavior;
import com.feed.sync.model.Item;
import com.feed.sync.observer.ItemObservable;
import com.feed.sync.observer.ItemObserver;
import com.feed.sync.validations.Guard;

/**
 * Main class that performs synchronization between two repositories.
 * 
 * @author jtondato
 */
public class SyncEngine {

	private final static MergeBehavior MERGE_BEHAVIOR = new MergeBehavior();

	// MODEL VARIABLES
	private ItemObservable itemReceivedObservable = new ItemObservable();
	private ItemObservable itemSentObservable = new ItemObservable();

	private Repository source;
	private Repository target;

	// BUSINESS METHODS
	public SyncEngine(Repository source, Repository target) {
		super();

		Guard.argumentNotNull(source, "left");
		Guard.argumentNotNull(target, "right");

		this.source = source;
		this.target = target;
	}

	// / <summary>
	// / Performs a full sync between the two repositories, automatically
	// / incorporating changes in both.
	// / </summary>
	// / <remarks>
	// / Items on the source repository are sent first, and then the
	// / changes from the target repository are incorporated into the source.
	// / </remarks>
	// / <returns>The list of items that had conflicts.</returns>
	public List<Item> synchronize() {
		return synchronize(null, NullPreviewHandler.INSTANCE,
				PreviewBehavior.None);
	}

	// / <summary>
	// / Performs a full sync between the two repositories, optionally calling
	// the
	// / given <paramref name="previewer"/> callback as specified by the
	// <paramref name="behavior"/> argument.
	// / </summary>
	// / <remarks>
	// / Items on the source repository are sent first, and then the
	// / changes from the target repository are incorporated into the source.
	// / </remarks>
	// / <returns>The list of items that had conflicts.</returns>
	public List<Item> synchronize(PreviewImportHandler previewer,
			PreviewBehavior behavior) {
		return synchronize(null, previewer, behavior);
	}

	// / <summary>
	// / Performs a partial sync between the two repositories since the
	// specified date, automatically
	// / incorporating changes in both.
	// / </summary>
	// / <param name="since">Synchronize changes that happened after this
	// date.</param>
	// / <remarks>
	// / Items on the source repository are sent first, and then the
	// / changes from the target repository are incorporated into the source.
	// / </remarks>
	// / <returns>The list of items that had conflicts.</returns>
	public List<Item> synchronize(Date since) {
		return synchronize(since, NullPreviewHandler.INSTANCE,
				PreviewBehavior.None);
	}

	// / <summary>
	// / Performs a partial sync between the two repositories since the
	// specified date, optionally calling the
	// / given <paramref name="previewer"/> callback as specified by the
	// <paramref name="behavior"/> argument.
	// / </summary>
	// / <param name="since">Synchronize changes that happened after this
	// date.</param>
	// / <remarks>
	// / Items on the source repository are sent first, and then the
	// / changes from the target repository are incorporated into the source.
	// / </remarks>
	// / <returns>The list of items that had conflicts.</returns>
	public List<Item> synchronize(Date since, PreviewImportHandler previewer,
			PreviewBehavior behavior) {

		Guard.argumentNotNull(previewer, "previewer");

		List<Item> sourceItems = (since == null) ? source.getAll() : source.getAllSince(since);
		List<Item> outgoingItems = this.enumerateItemsProgress(sourceItems, this.itemSentObservable);

		if (!target.supportsMerge()) {
			List<ItemMergeResult> outgoingToMerge = this.mergeItems(outgoingItems, target);
			if (behavior == PreviewBehavior.Right || behavior == PreviewBehavior.Both) {
				outgoingToMerge = previewer.preview(target, outgoingToMerge);
			}
			this.importItems(outgoingToMerge, target);
		} else {
			target.merge(outgoingItems);
		}

		List<Item> targetItmes = (since == null) ? target.getAll() : target.getAllSince(since);
		List<Item> incomingItems = this.enumerateItemsProgress(targetItmes, this.itemReceivedObservable);

		if (!source.supportsMerge()) {
			List<ItemMergeResult> incomingToMerge = this.mergeItems(incomingItems, source);
			if (behavior == PreviewBehavior.Left || behavior == PreviewBehavior.Both) {
				incomingToMerge = previewer.preview(source, incomingToMerge);
			}

			return this.importItems(incomingToMerge, source);
		} else {
			// If repository supports its own SSE merge behavior, don't apply it
			// locally.
			return new ArrayList<Item>(source.merge(incomingItems));
		}
	}

	private List<ItemMergeResult> mergeItems(List<Item> items,
			Repository repository) {

		ArrayList<ItemMergeResult> mergeResult = new ArrayList<ItemMergeResult>();
		for (Item incoming : items) {
			Item original = repository.get(incoming.getSyncId());
			ItemMergeResult result = MERGE_BEHAVIOR.merge(original, incoming);

			if (result.isMergeNone()) {
				mergeResult.add(result);
			}
		}
		return mergeResult;
	}

	private List<Item> importItems(List<ItemMergeResult> items,
			Repository repository) {
		// Straight import of data in merged results.
		// Conflicting items are saved and also
		// are returned for conflict resolution by the user or
		// a custom component. MergeBehavior determines
		// the winner element that is saved.
		// Conflicts are returned in a list because we need
		// the full iteration over the merged items to be
		// processed. If we returned an IEnumerable, we would
		// depend on the client iterating it in order to
		// actually import items, which is undesirable.
		ArrayList<Item> conflicts = new ArrayList<Item>();

		for (ItemMergeResult result : items) {
			if (result.isMergeNone() && result.getProposed() != null
					&& result.getProposed().hasSyncConflicts()) {
				conflicts.add(result.getProposed());
			}

			if (result.getOperation() == null
					|| result.getOperation().isRemoved()) {
				throw new UnsupportedOperationException();
			} else if (result.getOperation().isAdded()) {
				repository.add(result.getProposed());
			} else if (result.getOperation().isUpdated()
					|| result.getOperation().isConflict()) {
				repository.update(result.getProposed());
			}
		}

		return conflicts;
	}

	private List<Item> enumerateItemsProgress(List<Item> items,
			ItemObservable observable) {
		ArrayList<Item> result = new ArrayList<Item>();
		for (Item item : items) {
			result.add(item);
			observable.notifyObservers(item);
		}
		return result;
	}

	public void addItemReceivedObserver(ItemObserver observer) {
		this.itemReceivedObservable.addObserver(observer);
	}

	public void removeItemReceivedObserver(ItemObserver observer) {
		this.itemReceivedObservable.removeObserver(observer);
	}

	public void addItemSentObserver(ItemObserver observer) {
		this.itemSentObservable.addObserver(observer);
	}

	public void removeItemSentObserver(ItemObserver observer) {
		this.itemSentObservable.removeObserver(observer);
	}

}
