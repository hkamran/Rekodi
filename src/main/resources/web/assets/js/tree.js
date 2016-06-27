var animateTree = function () {
    $('.tree li:has(ul)').addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');

    var parent = $('.tree li.parent_li > div');
    parent.each(function(e) {
        var children = $(this).parent('li.parent_li').find(' > ul > li');
        if (children.is(":visible")) {
            children.show('fast');
        } else {
            children.hide('fast');
        }
    });
    var expand = $('.tree li.parent_li > div > i');
    expand.unbind('click');
    expand.on('click', function (e) {
        console.log(e);
        var span = $(this).parent();
        var children = span.parent('li.parent_li').find(' > ul > li');
        if (children.is(":visible")) {
            children.hide('fast');
            span.attr('title', 'Expand this branch').find(' > i').addClass('fa-caret-down').removeClass('fa-caret-up');
        } else {
            children.show('fast');
            span.attr('title', 'Collapse this branch').find(' > i').addClass('fa-caret-up').removeClass('fa-caret-down');
        }
        e.stopPropagation();
    });
};