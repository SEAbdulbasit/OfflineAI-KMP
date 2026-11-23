/** @OnlyCurrentDoc */
function addIntro() {
 const p=SlidesApp.getActivePresentation();
 if(p.getId()!=='13DefuAK4TPqDlWmC3ih0u-h6dqDOJkHpasNjHoWt0w4')throw new Error('Wrong deck');
 const old=p.getSlides(); if(old.length!==16)throw new Error('Expected 16 slides');
 const C={white:'#F4F7F5',accent:'#B7DBAD',muted:'#B8C9CE',panel:'#203E4F',line:'#45606C'};
 function text(s,v,x,y,w,h,size=22,color=C.white){const a=s.insertTextBox(v,x,y,w,h);a.getBorder().setTransparent();a.getText().getTextStyle().setFontFamily('Google Sans').setFontSize(size).setBold(false).setForegroundColor(color);a.getText().getParagraphStyle().setSpaceAbove(0).setSpaceBelow(0);return a;}
 function line(s,x,y,xx,yy,arrow=false){const a=s.insertLine(SlidesApp.LineCategory.STRAIGHT,x,y,xx,yy);a.getLineFill().setSolidFill(arrow?C.accent:C.line);a.setWeight(1.5);if(arrow)a.setEndArrow(SlidesApp.ArrowStyle.FILL_ARROW);}
 function box(s,v,x,y,w,h){const a=s.insertShape(SlidesApp.ShapeType.ROUND_RECTANGLE,x,y,w,h);a.getFill().setSolidFill(C.panel);a.getBorder().setTransparent();text(s,v,x+12,y+15,w-24,h-20,23);}
 function make(title,notes){const s=p.appendSlide(old[1]);s.getPageElements().forEach(e=>e.remove());text(s,title,54,34,612,47,30);line(s,60,94,660,94);s.getNotesPage().getSpeakerNotesShape().getText().setText(notes+'\n\n[Conference edition 2026-10-22]');return s;}
 let a=make('An LLM generates language from context','0:45\n\nLLM means large language model: training learns numerical weights from examples, and inference uses those weights to predict and generate tokens—pieces of text. A prompt can ask for a summary or extraction, but fluent output can still be wrong; the model does not execute device actions by itself.');
 text(a,'LLM = large language model',60,117,600,34,20,C.muted);
 box(a,'Your prompt',60,193,170,70);box(a,'Model',280,193,160,70);box(a,'Generated text',490,193,170,70);
 line(a,242,228,268,228,true);line(a,452,228,478,228,true);
 text(a,'Learns patterns → generates one token at a time',60,302,600,40,23,C.accent);
 a.move(1);
 let b=make('Offline inference keeps the work on-device','0:45\n\nOnce the model and required assets are on the phone, inference can run without a network request: prompts can stay local and there is no network round-trip or per-request cloud inference bill. This is not automatically faster or free: you pay in storage, RAM, battery and model limits, and the rest of the app must also avoid network dependencies.');
 box(b,'App',170,142,145,65);box(b,'Local model',380,142,190,65);line(b,328,174,368,174,true);
 text(b,'One phone · no server request',170,226,430,35,24,C.accent);
 text(b,'Works offline',60,300,200,32,23);text(b,'Data stays local',280,300,220,32,23);text(b,'No network wait',500,300,210,32,23);
 text(b,'Trade-off: storage / memory / battery',60,350,600,27,18,C.muted);
 b.move(2);
 const fmt=n=>Math.floor(n/60)+':'+String(n%60).padStart(2,'0');
 old.slice(1).forEach(s=>{const t=s.getNotesPage().getSpeakerNotesShape().getText();let v=t.asString();v=v.replace(/(\d+):(\d{2})–(\d+):(\d{2})/g,(_,a,b,c,d)=>fmt(+a*60 + +b+90)+'–'+fmt(+c*60 + +d+90)).replace('17:25 planned content','18:55 planned content');t.setText(v);});
 p.getSlides().slice(1).forEach((s,i)=>{s.getShapes().filter(x=>x.getTop()>=374 && /^\d{2}\s*$/.test(x.getText().asString())).forEach(x=>x.remove());text(s,String(i+2).padStart(2,'0'),635,375,30,18,10,C.muted);});
 console.log(JSON.stringify({count:p.getSlides().length,added:[a.getObjectId(),b.getObjectId()]}));
}
