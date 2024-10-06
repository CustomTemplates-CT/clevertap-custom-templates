package com.clevertap.ct_templates.nd.coachmark

interface SequenceListener{
    fun onNextItem(coachMark : CoachMarkOverlay, coachMarkSequence : CoachMarkSequence){
        coachMarkSequence.setNextView()
    }
}