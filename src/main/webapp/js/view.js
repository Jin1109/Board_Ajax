function getList(state){
   option=state;//현재 선택한 댓글 정렬방식을 저장합니다. 1=>등록순, 2=>최신순
   
   $.ajax({
      type:"post",
      url:"CommentList.bo",
      data : {"comment_board_num" : $("#comment_board_num").val() ,
              state:state},
      dataType:"json",
      success:function(rdata){
         $('#count').text(rdata.listcount).css('font-family','arial,sans-serif')
         var red1 = 'red';
         var red2 = 'red';
         if(option==1){
            red2='gray';
         }
         if(option==2){
            red1='gray';
         }
         var output="";
         
         if(rdata.boardlist.length>0){
            output += '<li class="comment-order-item ' + red1 + '" >'
                   + '   <a href="javascript:getList(1)" class="comment-order-button">등록순 </a>'
                   + '</li>'
                   + '<li class="comment-order-item ' + red2 + '" >'
                   + '   <a href="javascript:getList(2)" class="comment-order-button">최신순 </a>'
                   + '</li>'
              $('.comment-order-list').html(output);
              output='';
             $(rdata.boardlist).each(function(){
               var lev = this.comment_re_lev;
               var comment_reply='';
               if(lev==1){
                  comment_reply = ' comment-list-item--reply lev1';
               }else if(lev==2){
                  comment_reply = ' comment-list-item--reply lev2';
               }
               var profile=this.memberfile;
               var src='image/profile.png';
               if(profile){
                  src='memberupload/'+profile;
               }
               
               output += '<li id="' + this.num + '" class="comment-list-item' + comment_reply + '" >'
                     + '   <div class="comment-nick-area">'
                     + '      <img src="' + src + '" alt="프로필 사진" width="36" height="36">'
                     + '      <div class="comment-box">'
                     + '        <div class="comment-nick-box">'
                     + '              <div class="comment-nick-info">'
                     + '                 <div class="comment-nickname">' + this.id + '</div>'
                     + '              </div>' //comment-nick-info
                     + '        </div>' //comment-nick-box
                     + '      </div>' //comment-box
                     + '      <div class="comment-text-box">'
                     + '         <p class="comment-text-view">'
                     + '           <span class="text-comment">' + this.content + '</span>'
                     + '         </p>'
                     + '      </div>' //comment-text-box
                     + '      <div class="comment-info-box">'
                     + '        <span class="comment-info-date">' + this.reg_date + '</span>';
               if(lev<2){
                  output += '   <a href="javascript:replyform(' + this.num +','
                         + lev + ',' + this.comment_re_seq + ','
                         + this.comment_re_ref + ')" class="comment-info-buttom">답글쓰기</a>'
                  }
               output += '   </div>' //comment-info-box;
               
               if($("#loginid").val()==this.id){  
					output +=  '<div class="comment-tool">'
					+ '    <div title="더보기" class="comment-tool-button">'
					+ '       <div>&#46;&#46;&#46;</div>' 
					+ '    </div>'
					+ '    <div id="comment-list-item-layer' +  this.num + '"  class="LayerMore">' //스타일에서 display:none; 설정함
					+ '     <ul class="layer-list">'							   
					+ '      <li class="layer-item">'
					+ '       <a href="javascript:updateForm(' + this.num + ')"'
					+ '          class="layer-button">수정</a>&nbsp;&nbsp;'
					+ '       <a href="javascript:del(' + this.num + ')"'
					+ '          class="layer-button">삭제</a></li></ul>'
					+ '    </div>'//LayerMore
					+ '   </div>'//comment-tool
}
               
               output += '</div>'// comment-nick-area
                     + '</li>' //li.comment-list-item
            })//each
            
            $('.comment-list').html(output);
         }//if(radat.boardlist.length>0)
         else{ //댓글 1개가 있는 상태에서 삭제하는 경우 갯수는 0이라 if문을 수행하지 않고 이곳으로 옵니다.
               //이곳에서 아래의 두영역을 없앱니다.
               $('.comment-list').empty();
               $('.comment-order-list').empty();
         }
      }//success end
   });//ajax end
}//function(getList) end

//더보기 - 수정 클릭한 경우에 수정 폼을 보여줍니다.
function updateForm(num){ //num : 수정할 댓글 글번호
	
	var $num = $('#'+num);
	
	//선택한 내용을 구합니다.
	var content=$num.find('.text-comment').text();
	
	var selector = '#'+num + '> .comment-nick-area'
	$(selector).hide(); //selector 영역 숨겨요-수정에서 취소를 선택하면 보여줄 예정입니다.
	
	//$('.comment-list+.comment-write').clone() : 기본 글쓰기 영역 복사합니다.
	//글이 있던 영역에 글을 수정할 수 있는 폼으로 바꿉니다.
	$num.append($('.comment-list+.commnet-write').clone());
	
	//수정 폼의 <textarea>에 내용을 나타냅니다.
	$num.find('textarea').val(content);
	
	//'.btn-register' 영역에 수정할 글 번호를 속성 'data-id'에 나타내고 클래스 'update'를 추가합니다.
	$num.find('.btn-register').attr('data-id',num).addClass('update').text('수정완료');
	
	//폼에서 취소를 사용할 수 있도록 보이게 합니다.
	$num.find('.btn-cancel').css('display','block');
	
	var count=content.length;
	$num.find('.comment-write-area-count').text(count+"/200");
}//function(updateForm) end

//더보기 -> 삭제 클릭한 경우 실행하는 횟수
function del(num){ //num : 댓글 번호
	if(!confirm('정말 삭제하시겠습니까')){
		$('#comment-list-item-layer' + num).hide(); //'수정 삭제' 영역 숨겨요
		return;
	}
	
		$.ajax({
			url:'CommentDelete.bo',
			data:{num:num},
			success:function(rdata){
				if(rdata==1){
					getList(option);
				}
			}
		})
}//function(del) end

//답글 달기 폼
function replyform(num,lev,seq,ref){
	var output = '<li class="comment-list-item comment-list-item--reply lev' 
		         +  lev    + ' comment-list-item-form"></li>' 
    var $num = 	$('#'+num);	         
	//선택한 글 뒤에 답글 폼을 추가합니다.
	$num.after(output); 
	
	//글쓰기 영역 복사합니다.
	output=$('.comment-list+.commnet-write').clone();
	
	var $num_next = $num.next();
	//선택한 글 뒤에 답글 폼 생성합니다.
	$num_next.html(output);
	//답글 폼의  <textarea>의 속성 'placeholder'를 '답글을 남겨보세요'로 바꾸어 줍니다.
	$num_next.find('textarea').attr('placeholder','답글을 남겨보세요');
	
	//답글 폼의  '.btn-cancel'을 보여주고 클래스 'reply_cancel'를 추가합니다.
	$num_next.find('.btn-cancel').css('display','block').addClass('reply_cancel');
	
	//답글 폼의 '.btn-register'에  클래스 'reply' 추가합니다.
	//속성 'data-ref'에 ref, 'data-lev'에 lev, 'data-seq'에 seq값을 설정합니다.
	//등록을 답글 완료로 변경합니다.
	$num_next.find('.btn-register').addClass('reply')
	         .attr('data-ref',ref).attr('data-lev',lev).attr('data-seq',seq).text('답글완료');

}//function(replyform) end


$(function(){
   option=1;
   getList(option); //처음 로드 될 때는 등록순 정렬
   
   $('form[name="deleteForm"]').submit(function(){
      if ($("#board_pass").val() == ''){
         alert("비밀번호를 입력하세요");
         $("#board_pass").focus();
         return false;
      }
   })//form
   
   
   $('.comment-area').on('keyup','.comment-write-area-text',function(){
      var length=$(this).val().length;
      $(this).prev().text(length+'/200');
   });// keyup','.comment-write-area-text',function() {
   
   //댓글 등록을 클릭하면 데이터베이스에 저장 -> 저장 성공 후에 리스트 불러옵니다.
   $('ul+.commnet-write .btn-register').click(function() {
      var content=$('.comment-write-area-text').val();
      if(!content){//내용 없이 등록한 경우
         alert("댓글을 입력하세요");
         return;
      }
      
      $.ajax({
         url : 'CommentAdd.bo',   //원문 등록
         data : {
            id : $("#loginid").val(),
            content : content,
            comment_board_num : $("#comment_board_num").val(),
            comment_re_lev : 0,     //원문인 경우 comment
                             //comment_re_ref는 댓글의 원문 글번호
            comment_re_seq : 0
         },
         type : 'post',
         success : function(rdata) {
            if (rdata == 1) {
               getList(option);
            }
            
         }
      })//ajax
      
      
      $('.comment-write-area-text').val('');//textarea 초기화
      $('.comment-write-area-count').text('0/200');//입력한 글 카운트 초기화
   })// $('.btn-register').click(function(){
   
  
   //더보기를 클릭하면 수정과 삭제 영역이 나타나고 다시 클릭하면 사라져요
	$(".comment-list").on('click', '.comment-tool-button', function() {
		
		var  LayerMore = $(this).next();
		if(LayerMore.css('display')=='block'){  //보이면
			LayerMore.hide().removeClass('show'); //보이지 않도록 합니다.
		}else{ //보이지 않은 경우
			$(".show").hide(); //다른 열려있는 show 클래스를 갖는 엘리먼트는 숨기고 
			LayerMore.show().addClass('show');//현재 선택한 것만 보이도록 합니다.
		}
	})

//수정 후 수정완료를 클릭한 경우
$('.comment-area').on('click','.update',function(){
	var num = $(this).attr('data-id');
	var content = $(this).parent().parent().find('textarea').val();
	$.ajax({
		url:'CommentUpdate.bo',
		data:{num:num, content:content},
		success:function(rdata){
			if(rdata==1){
				getList(option);
			}//if
		}//success
	});//ajax
})//수정 후 수정완료를 클릭한 경우

//수정 후 취소 버튼을 클릭한 경우
$('.comment-area').on('click','.btn-cancel',function(){
	//댓글 번호를 구합니다.
	var num= $(this).next().attr('data-id');
	var selector='#' +num;
	
	//.commnet-write 영역 삭제 합니다.
	$(selector + '.commnet-write').remove();
	
	//숨겨두었던 .comment-nick-area 영역 보여줍니다.
	$(selector + '>.comment-nick-area').css('display','block');
	
	//숨겨두었던 .comment-nick-area 영역 보여주면 '수정 삭제'영역도 보입니다.
	//console.log($('#comment-list-item-layer' + num).css('display')) //'block'
	//$('#comment-list-item-layer' + num).hide(); //'수정 삭제' 영역 숨겨요
})//수정 후 취소 버튼을 클릭한 경우
   
//답글완료 클릭한 경우
	$('.comment-area').on('click','.reply',function(){
		var comment_re_ref = $(this).attr('data-ref');
		var content=$(this).parent().parent().find('.comment-write-area-text').val();
		var lev = $(this).attr('data-lev');
		var seq = $(this).attr('data-seq');
		$.ajax({
			url : 'CommentReply.bo',  
			data : {
				id : $("#loginid").val(),
				content : content,
				comment_board_num : $("#comment_board_num").val(),
				comment_re_lev : lev, 
				comment_re_ref : comment_re_ref,
				comment_re_seq : seq
			},
			type : 'post',
			success : function(rdata) {
				if (rdata == 1) {
					getList(option);
				}
			}
		})//ajax
		
	})//답글완료 클릭한 경우
	
	//답글쓰기 후 취소 버튼을 클릭한 경우
	$('.comment-area').on('click','.reply_cancel',function(){
		$(this).parent().parent().parent().remove();
	})//답글쓰기 후 취소 버튼을 클릭한 경우

});