package pyxis.uzuki.live.naraeimagepicker.fragment

import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import kotlinx.android.synthetic.main.fragment_list.*
import pyxis.uzuki.live.naraeimagepicker.base.BaseFragment
import pyxis.uzuki.live.naraeimagepicker.fragment.adapter.AlbumAdapter
import pyxis.uzuki.live.naraeimagepicker.item.AlbumItem
import pyxis.uzuki.live.naraeimagepicker.item.FileFilter
import pyxis.uzuki.live.naraeimagepicker.module.SelectedItem
import pyxis.uzuki.live.naraeimagepicker.utils.TimeLogger
import pyxis.uzuki.live.naraeimagepicker.utils.getColumnString
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import pyxis.uzuki.live.richutilskt.utils.toFile

/**
 * NaraeImagePicker
 * Class: AlbumFragment
 * Created by Pyxis on 1/6/18.
 *
 * Description:
 */

class AlbumFragment : BaseFragment<AlbumItem>() {
    private lateinit var adapter: AlbumAdapter
    private val itemList = arrayListOf<AlbumItem>()
    private lateinit var timeLogger: TimeLogger

    override fun getItemList() = itemList
    override fun getItemKind() = AlbumItem::class.simpleName ?: ""

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timeLogger = TimeLogger(AlbumFragment::class.java.simpleName, "loadItem")
        timeLogger.addPart("start")
        adapter = AlbumAdapter(activity, itemList)
        recyclerView.adapter = adapter

        runAsync { loadItem() }
    }

    private fun loadItem() {
        timeLogger.addPart("loadItem")
        val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)

        val selection = when (SelectedItem.getFilter()) {
            FileFilter.NONE -> null
            FileFilter.PNG -> MediaStore.Files.FileColumns.MIME_TYPE + "=?"
            FileFilter.JPEG -> MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?"
        }

        val selectionArg = when (SelectedItem.getFilter()) {

            FileFilter.NONE -> null
            FileFilter.PNG -> arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension(".png"))
            FileFilter.JPEG -> arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension(".jpg"),
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(".jpeg"))
        }

        val cursor = activity.contentResolver.query(cursorUri, projection, selection, selectionArg, orderBy)
        val items = HashSet<AlbumItem>()

        timeLogger.addPart("before use")
        timeLogger.addPart("now using cursor")
        if (cursor.moveToFirst()) {
            timeLogger.addPart("moveToFirst")
            while (cursor.moveToNext()) {
                val album = cursor.getColumnString(displayNameColumn)
                val image = cursor.getColumnString(pathColumn)
                val file = image.toFile()

                if (!file.exists()) continue

                items.add(AlbumItem(album, image))
            }
            timeLogger.addPart("looping end")
        }

        cursor.close()
        itemList.addAll(items)
        timeLogger.addPart("addAll")
        items.clear()
        runOnUiThread {
            timeLogger.addPart("notify")
            recyclerView.notifyDataSetChanged()
            timeLogger.println()
        }
    }
}