package com.rank.football.cast

import android.os.Bundle
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity

/** Full-screen Cast expanded controller activity. */
class ExpandedControlsActivity : ExpandedControllerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        CastContext.getSharedInstance(this)
        super.onCreate(savedInstanceState)
    }
}
