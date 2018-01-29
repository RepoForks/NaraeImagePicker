package pyxis.uzuki.live.naraeimagepicker.module

import pyxis.uzuki.live.naraeimagepicker.Constants
import pyxis.uzuki.live.naraeimagepicker.item.FileFilter
import pyxis.uzuki.live.naraeimagepicker.item.ImageItem

/**
 * NaraeImagePicker
 * Class: SelectedItem
 * Created by Pyxis on 1/6/18.
 *
 * Description:
 */

object SelectedItem {
    private val items: ArrayList<ImageItem> = arrayListOf()
    private var count = Constants.LIMIT_UNLIMITED
    private var filter: FileFilter = FileFilter.NONE

    fun addItem(item: ImageItem, listener: (Boolean) -> Unit) {
        if (count != Constants.LIMIT_UNLIMITED && items.size == count) {
            listener.invoke(false)
            return
        }

        items.add(item)
        listener.invoke(true)
    }

    fun removeItem(item: ImageItem) {
        items.remove(item)
    }

    fun contains(item: ImageItem) = items.contains(item)

    fun setLimits(limit: Int) {
        this.count = limit
    }

    fun getLimits() = this.count

    fun getImageList(): ArrayList<String> = ArrayList<String>().apply {
        addAll(items.map { it.imagePath }.toList())
    }

    fun isNotEmpty() = items.isNotEmpty()

    fun clear() {
        items.clear()
        items.trimToSize()
    }

    fun setFilter(fileFilter: FileFilter) {
        this.filter = fileFilter
    }

    fun getFilter(): FileFilter {
        return filter
    }
}