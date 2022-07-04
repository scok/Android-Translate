package com.example.translation.ui.home

import org.json.JSONObject

class PapagoEntity {
    var data : ResultMessage? = null

    inner class ResultMessage{
        var renderedImage : String? = null
    }
}