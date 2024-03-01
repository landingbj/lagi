/*
*    This program is commercial software; you can only redistribute it and/or modify
*    it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
*
*    You should have received a copy license along with this program;
*    If not, write to Beijing Landing Technologies, service@landingbj.com.
*/

/*
*    login.js
*    Copyright (C) 2018 Beijing Landing Technologies, China
*
*/
$(function () {
    /*动画函数*/
    $.Velocity.RegisterEffect('transition.swoopInCenter', {
            defaultDuration: 850,
            calls: [
                [{
                    opacity: [1, 0],
                    transformOriginX: ["-100%", "50%"],
                    transformOriginY: ["-100%", "100%"],
                    scaleX: [1, 0],
                    scaleY: [1, 0],
                    translateY: ['-50%', -700],
                    translateX: ['-50%', -700],
                    translateZ: 0
                }]
            ],
            reset: {transformOriginX: "50%", transformOriginY: "50%"}
        }
    ).RegisterEffect('transition.swoopOutCenter', {
            defaultDuration: 850,
            calls: [
                [{
                    opacity: [0, 1],
                    transformOriginX: ["50%", "-100%"],
                    transformOriginY: ["100%", "-100%"],
                    scaleX: 0,
                    scaleY: 0,
                    translateZ: 0
                }]
            ],
            reset: {transformOriginX: "50%", transformOriginY: "50%"}
        }
    );
    $.fn.extend({
        dialogIn: function () {
            return this.velocity('transition.bounceDownIn');
        },
        dialogOut: function () {
            return this.velocity('transition.bounceUpOut');
        },
        popupIn: function () {
            return this.velocity('transition.swoopInCenter', {display: 'block'});
        },
        popupOut: function () {
            return this.velocity('transition.swoopOutCenter', {display: 'none'});
        },
        rotateUp: function () {
            this.velocity("stop");
            this.velocity({
                rotateZ: '180deg'
            }, {
                duration: 300
            });
        },
        rotateDown: function () {
            this.velocity("stop");
            this.velocity({
                rotateZ: '0deg'
            }, {
                duration: 300
            });
        },
        toggle: function (state) {
            if (typeof state === "boolean") {
                return state ? this.show() : this.hide();
            }

            return this.each(function () {
                if (isHidden(this)) {
                    $(this).show();
                } else {
                    $(this).hide();
                }
            });
        }
    });

    $('body').particleground({
        dotColor: '#E8DFE8',
        lineColor: '#133b88'
    });
    $('.pg-canvas').animate({
        opacity: 1
    }, 4000);
});
