const main = {
    init: function () {
        const _this = this;
        $('#btn-save').on('click', function () {
            _this.save();
        });

        $('#btn-update').on('click', function () {
            _this.update();
        })
        $('#btn-delete').on('click', function () {
            _this.delete();
        })
    },

    save: function () {
        const data = {
            title: $('#title').val(),
            author: $('#author').val(),
            content: $('#content').val()
        };

        $.ajax({
            type: 'POST',
            url: '/api/v1/posts',
            dataType: 'json',
            contentType: 'application/json; charset= utf-8',
            data: JSON.stringify(data)
        }).done(function () {
            alert('등록되었습니다.');
            window.location.href = '/';  //등록 성공하면 메인페이지(/)로 이동한다.
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });

    },

    update : function () {
        const data = {
            title: $('#title').val(),
            content: $('#content').val()
        };

        const id = $('#id').val();

        $.ajax({
            type: 'PUT',
            url: '/api/v1/posts/' + id,
            dataType: 'json',
            contentType: 'application/json; charset= utf-8',
            data: JSON.stringify(data)
        }).done(function () {
            alert('수정되었습니다.');
            window.location.href = '/';  //등록 성공하면 메인페이지(/)로 이동한다.
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    },

    delete : function () {
        const id = $('#id').val();

        $.ajax({
            type: 'DELETE',
            url: '/api/v1/posts/' + id,
            dataType: 'json',
            contentType: 'application/json; charset= utf-8'
        }).done(function () {
            alert('삭제되었습니다.');
            window.location.href = '/';  //삭제 후 메인페이지(/)로 이동한다.
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    }

}
main.init();