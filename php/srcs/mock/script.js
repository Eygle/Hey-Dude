/**
 * Created by Johan on 06/03/2015.
 */

function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return s4() + s4() + s4() + s4() + s4() + s4() + s4() + s4();
}

function refresh() {
    var $body = $('body');
    $body.empty();

    jQuery.getJSON('mock_known_users', function(data) {
        for (var i in data) {
            var $content = $('<div>').attr({id:i, class:"content"});
            $('<div>').attr('class', 'crop').append($('<img>').attr({'class': 'img', src: data[i].image})).appendTo($content);
            var $texts = $('<div>').attr('class', 'texts').appendTo($content);
            $('<div>').attr('class', 'name').html("<b>Name: </b>" + data[i].name).appendTo($texts);
            $('<div>').attr('class', 'id').html("<b>gId: </b>" + data[i].gId).appendTo($texts);
            $('<div>').attr('class', 'email').html("<b>Email: </b>" + data[i].email).appendTo($texts);
            $('<div>').attr('class', 'ip').html("<b>IP: </b>" + data[i].IP).appendTo($texts);
            $('<div>').attr('class', 'key').html("<b>Key: </b>" + data[i].publicKey).appendTo($texts);

            $('<div>').attr('class', 'remove').text('X').click(function(d) {
                var u = data[$(this).parent().attr('id')];

                if (!confirm("Do you really want to remove " + u.name + " ?")) return;

                $.post('../api.php', {
                    action: "remove",
                    mock: true,
                    gId: u.gId
                }, function() {
                    refresh();
                });
            }).appendTo($content);

            $('<button>').attr('class', 'fst_btn green').text("Set online").click(function() {
                var u = data[$(this).parent().parent().attr('id')];

                $.post('../api.php', {
                    action: "login",
                    mock: true,
                    gId: u.gId,
                    name: u.name,
                    image: u.image,
                    email: u.email,
                    publicKey: u.publicKey
                });
            }).appendTo($texts);

            $('<button>').attr('class', 'fst_btn red').text("Set offline").click(function() {
                var u = data[$(this).parent().parent().attr('id')];

                $.post('../api.php', {
                    action: "logout",
                    mock: true,
                    gId: u.gId
                });
            }).appendTo($texts);

            $('<button>').attr('class', 'div').text("Call").click(function() {
                var u = data[$(this).parent().parent().attr('id')];

                $.post('../api.php', {
                    action: "call",
                    mock: true,
                    gId: u.gId,
                    destGId: "42"
                });
            }).appendTo($texts);

            $('<button>').attr('class', 'green').text("Accept call").click(function() {
                var u = data[$(this).parent().parent().attr('id')];

                $.post('../api.php', {
                    action: "answer",
                    mock: true,
                    gId: u.gId,
                    destGId: "42",
                    status: "accept"
                });
            }).appendTo($texts);

            $('<button>').attr('class', 'red').text("Refuse call").click(function() {
                var u = data[$(this).parent().parent().attr('id')];

                $.post('../api.php', {
                    action: "answer",
                    mock: true,
                    gId: u.gId,
                    destGId: "42",
                    status: "refuse"
                });
            }).appendTo($texts);

            $body.append($content);
        }

        var $content = $('<div>').attr('class', "content");

        $('<div>').attr('class', 'crop').append($('<img>').attr({id: 'img_form_preview', class: 'img', src: ""})).appendTo($content);
        var $texts = $('<div>').attr('class', 'texts').appendTo($content);

        var $img = $('<div>').html("<div class='label'>Image link: </div>").appendTo($texts);
        $('<input>').attr({class: 'img_form', type:'text'}).on('input',function() {
            $('#img_form_preview').attr('src', $(this).val());
        }).appendTo($img);

        var $name = $('<div>').html("<div class='label'>Name: </div>").appendTo($texts);
        $('<input>').attr({class: 'name_form', type:'text'}).appendTo($name);

        var $email = $('<div>').html("<div class='label'>Email: </div>").appendTo($texts);
        $('<input>').attr({class: 'email_form', type:'text'}).appendTo($email);

        $('<button>').attr('class', 'fst_btn div').text("Add and set online").click(function() {
            var name = $('.name_form').val();
            var img = $('.img_form').val();
            var email = $('.email_form').val();

            $.post('../api.php', {
                action: "login",
                mock: true,
                gId: guid(),
                name: name,
                image: img,
                email: email,
                publicKey: "toto"
            }, function(osef) {
                refresh();
            });
        }).appendTo($texts);

        $body.append($content);
    });
}

$(document).ready(function() {
    refresh();
});