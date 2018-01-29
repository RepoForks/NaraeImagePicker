package pyxis.uzuki.live.naraeimagepicker.fragment

import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import kotlinx.android.synthetic.main.fragment_list.*
import pyxis.uzuki.live.naraeimagepicker.Constants
import pyxis.uzuki.live.naraeimagepicker.base.BaseFragment
import pyxis.uzuki.live.naraeimagepicker.event.ToolbarEvent
import pyxis.uzuki.live.naraeimagepicker.fragment.adapter.ImageAdapter
import pyxis.uzuki.live.naraeimagepicker.item.FileFilter
import pyxis.uzuki.live.naraeimagepicker.item.ImageItem
import pyxis.uzuki.live.naraeimagepicker.module.SelectedItem
import pyxis.uzuki.live.naraeimagepicker.utils.getColumnString
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import pyxis.uzuki.live.richutilskt.utils.toFile

/**
 * NaraeImagePicker
 * Class: ImageFragment
 * Created by Pyxis on 1/6/18.
 *
 * Description:
 */

class ImageFragment : BaseFragment<ImageItem>() {
    private lateinit var adapter: ImageAdapter
    private val itemList = arrayListOf<ImageItem>()

    private var albumName = ""

    override fun getItemList() = itemList
    override fun getItemKind() = ImageItem::class.simpleName ?: ""

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        albumName = arguments.getString(Constants.EXTRA_NAME)
        sendEvent(ToolbarEvent(albumName, true))

        adapter = ImageAdapter(activity, itemList)
        recyclerView.adapter = adapter
        runAsync { loadItem() }
    }

    private fun loadItem() {
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)

        val selection = when (SelectedItem.getFilter()) {
            FileFilter.NONE -> MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =? "
            FileFilter.PNG -> MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =? AND " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
            FileFilter.JPEG -> MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =? AND " + "(" + MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?" + ")"
        }

        val selectionArg = when (SelectedItem.getFilter()) {

            FileFilter.NONE -> arrayOf(albumName)
            FileFilter.PNG -> arrayOf(albumName, MimeTypeMap.getSingleton().getMimeTypeFromExtension(".png"))
            FileFilter.JPEG -> arrayOf(albumName, MimeTypeMap.getSingleton().getMimeTypeFromExtension(".jpg"),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(".jpeg"))
        }

        val cursor = activity.contentResolver.query(cursorUri, projection, selection, selectionArg, orderBy)
        val items = HashSet<ImageItem>()

        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                val image = cursor.getColumnString(pathColumn)
                val id = cursor.getColumnString(idColumn)
                val file = image.toFile()

                if (!file.exists()) continue

                items.add(ImageItem(id, image))
            }
        }

        cursor.close()

        itemList.addAll(items)
        items.clear()

        runOnUiThread {
            recyclerView.notifyDataSetChanged()
            sendEvent(ToolbarEvent("$albumName (${itemList.size})", true))
        }
    }
}