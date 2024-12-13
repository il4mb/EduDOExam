package com.il4mb.edudoexam.api.response

class ResponseError(error: Boolean, message: String, val code: Int) :Response(error, message) {
    companion object {
        fun from(code: Int, response: Response?): ResponseError {
            return ResponseError(response?.error ?: true, response?.message ?: "Unknown Error", code)
        }

    }
}