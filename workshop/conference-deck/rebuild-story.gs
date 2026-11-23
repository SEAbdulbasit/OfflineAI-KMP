/** @OnlyCurrentDoc */
function rebuildStory(){
 const p=SlidesApp.getActivePresentation();if(p.getId()!=='13DefuAK4TPqDlWmC3ih0u-h6dqDOJkHpasNjHoWt0w4')throw new Error('Wrong deck');
 const old=p.getSlides();if(old.length!==18)throw new Error('Expected 18');
 const C={white:'#F4F7F5',accent:'#B7DBAD',muted:'#B8C9CE',panel:'#203E4F',dark:'#102632',line:'#45606C'};
 const out=[]; const dur=[];
 function text(s,v,x,y,w,h,z=23,c=C.white,font='Google Sans'){let a=s.insertTextBox(v,x,y,w,h);a.getBorder().setTransparent();a.getText().getTextStyle().setFontFamily(font).setFontSize(z).setBold(false).setForegroundColor(c);a.getText().getParagraphStyle().setSpaceAbove(0).setSpaceBelow(0);return a;}
 function line(s,x,y,xx,yy,arrow=false){let a=s.insertLine(SlidesApp.LineCategory.STRAIGHT,x,y,xx,yy);a.getLineFill().setSolidFill(arrow?C.accent:C.line);a.setWeight(1.5);if(arrow)a.setEndArrow(SlidesApp.ArrowStyle.FILL_ARROW);}
 function rect(s,x,y,w,h,c=C.panel){let a=s.insertShape(SlidesApp.ShapeType.ROUND_RECTANGLE,x,y,w,h);a.getFill().setSolidFill(c);a.getBorder().setTransparent();}
 function make(title,n,seconds=45){let s=p.appendSlide(old[3]);s.getPageElements().forEach(e=>e.remove());text(s,title,54,34,612,47,29);line(s,60,94,660,94);s.getNotesPage().getSpeakerNotesShape().getText().setText(n);out.push(s);dur.push(seconds);return s;}
 function copy(i,seconds){let s=p.appendSlide(old[i]);out.push(s);dur.push(seconds);return s;}
 function code(s,v,y=145,h=155,z=18){rect(s,60,y-20,600,h,C.dark);text(s,v,76,y,568,h-20,z,C.accent,'Roboto Mono');}
 let s=copy(0,25);
 s=copy(1,40);s.replaceAllText('Your prompt','“Light on”');s.replaceAllText('Text output','Text reply');
 s.getNotesPage().getSpeakerNotesShape().getText().appendText('\nOur running example is “Turn on the flashlight.” At this point the model only generates text; nothing on the phone has changed. Training learns numerical weights, inference uses them.');
 s=copy(2,45);
 s=make('Start with a narrow task—not an assistant', 'Use the flashlight as a bounded experiment with one Boolean argument. Small models can choose the wrong action, invent an argument or act on an unsupported request; use measured task results rather than broad claims about summarization or assistants.\nUse deterministic logic when fixed commands are sufficient; an LLM is useful when flexible natural-language input justifies its cost.',50);
 text(s,'A task we can test',60,130,290,38,25,C.accent);text(s,'A promise we cannot make',375,130,300,38,23,C.muted);
 text(s,'One tool\nOn / off / no action',60,195,280,100,26);text(s,'Any question\nAlways correct',375,195,280,100,26);
 text(s,'Wrong tool? Missing argument? Take no action.',60,325,600,35,21,C.accent);
 s=make('Tokens are pieces of the conversation','A token is a unit chosen by the model’s tokenizer, not necessarily a word. The boxes are conceptual parts of the prompt, not an exact tokenization; instructions, tool descriptions and conversation consume the available context, and output also needs a budget.',50);
 text(s,'The model has a finite context budget',60,125,600,35,24,C.muted);
 [['Instructions',60,175],['Tool definition',255,175],['User request',465,175]].forEach(([v,x,y])=>{rect(s,x,y,190,70);text(s,v,x+10,y+19,175,40,20);});
 line(s,60,276,655,276);text(s,'More history → more processing + cache memory',60,300,610,45,23,C.accent);
 s=copy(6,55);s.replaceAllText('The phone sets the resource budget','Weights take space; inference needs more');
 s.getNotesPage().getSpeakerNotesShape().getText().appendText('\nWeights are the learned numerical parameters. B means billion parameters. These bars are arithmetic for one billion weights, not downloadable artifact sizes or peak RAM: 1B × 16 bits ≈ 2 GB; 1B × 4 bits ≈ 0.5 GB. Quantization uses lower precision and may change accuracy. Runtime buffers and context cache add memory.');
 s=copy(5,40);
 s=make('Choose a model for the job','These are two concrete examples, not the complete landscape. Gemma 3 1B is a text model to evaluate for prompted tasks; FunctionGemma 270M is designed for task-specific tool-calling fine-tuning and is not a general dialogue model. Check compatible runtime artifacts, chat template, license and measured accuracy on the target device.\nSources: https://developers.google.com/edge/litert-lm/overview ; https://ai.google.dev/gemma/docs/functiongemma/model_card',45);
 text(s,'Gemma 3 · 1B',60,138,290,44,28,C.accent);text(s,'FunctionGemma · 270M',365,138,310,44,25,C.accent);
 text(s,'Text generation\nPrompt + evaluate',60,212,280,90,23);text(s,'Tool-call specialization\nFine-tune + evaluate',365,212,310,90,23);
 text(s,'A smaller file is not proof of a better result',60,330,610,36,22,C.muted);
 s=copy(7,40);
 s=copy(8,50);s.getNotesPage().getSpeakerNotesShape().getText().appendText('\nMigration lesson: shared GemmaInference callers remained stable while the iOS native implementation changed from MediaPipe to LiteRT-LM. Platform initialization, cancellation and cleanup still require separate testing.');
 s=make('The shared contract stays small','Real Kotlin declarations excerpted from commonMain/inference/GemmaInference.kt; other members are omitted. The config parameter type is imported from the project, and Flow from kotlinx.coroutines.flow. Android prepares the engine while iOS currently resolves the path and initializes lazily; the same interface does not guarantee identical lifecycle behavior.',55);
 code(s,'expect class GemmaInference() {\n    suspend fun loadModel(\n        modelPath: String, config: ModelConfig = ModelConfig())\n    fun generateResponse(prompt: String): Flow<String>\n}',145,170,15);
 text(s,'Prepare a model → stream text into shared UI',60,330,610,35,23,C.accent);
 s=copy(11,50);
 s=make('A tool needs a name, purpose and arguments','This is a readable representation of the real ToggleTorchTool specification, not a compilable Kotlin snippet. The source declares the name and description plus a JSON schema with a required Boolean enable property and additionalProperties=false. The app must supply this definition before asking the model to select the tool.\nSource: commonMain/tools/Tooling.kt, ToggleTorchTool.spec.',45);
 text(s,'NAME',60,133,150,27,15,C.muted);text(s,'toggle_torch',240,128,420,40,26,C.accent,'Roboto Mono');
 text(s,'PURPOSE',60,211,150,27,15,C.muted);text(s,'Turn the flashlight on or off',240,206,425,45,23);
 text(s,'ARGUMENT',60,289,150,27,15,C.muted);text(s,'enable: true | false',240,284,420,45,23,C.accent,'Roboto Mono');
 s=copy(10,50);
 s=copy(12,45);
 s=copy(13,60);
 s=copy(14,55);
 s=make('Demo: inspect the call, then the device','LIVE DEMO PLAN, NOT MEASURED RESULTS. Use the exact installed build, compatible model and a physical phone with a torch. First show the model file loaded and network disabled, then inspect the generated proposal, validation and native result before observing the torch.\nRun on, off and an unsupported request; explain any failure honestly. Native acceptance is not independent verification of the physical torch. A successful run has not been established by this deck. Keep a rehearsed recording as fallback.\nSuggested 2:30: setup 20s, on 40s, off 30s, unsupported request 40s, limitation 20s.',150);
 text(s,'Model ready · airplane mode · Wi-Fi off',60,123,600,35,21,C.muted);
 [['“Turn on the flashlight”','enable: true'],['“Turn it off”','enable: false'],['Unsupported request','No device action']].forEach((v,i)=>{text(s,v[0],60,190+i*58,355,37,24);text(s,v[1],440,190+i*58,235,37,21,C.accent);});
 s=make('Ship only what you can measure','Evaluate the exact model, prompt, quantization, runtime and device combination. Record a success rate over held-out requests, including paraphrases, ambiguous input, malformed calls and no-action requests; report latency and memory alongside correctness. If prompting and integration are correct but accuracy is inadequate, test task-specific fine-tuning or use a simpler deterministic feature.',45);
 text(s,'Correct tool + arguments',60,140,600,43,28);
 text(s,'No action when the request is unsupported',60,218,610,43,25);
 text(s,'First-token latency / memory / failures',60,296,610,43,25,C.accent);
 s=copy(17,30);
 const fmt=n=>Math.floor(n/60)+':'+String(n%60).padStart(2,'0');let elapsed=0;
 out.forEach((s,i)=>{s.getShapes().filter(x=>x.getTop()>=374 && /^\d{2}\s*$/.test(x.getText().asString())).forEach(x=>x.remove());if(i)text(s,String(i+1).padStart(2,'0'),635,375,30,18,10,C.muted);
 let n=s.getNotesPage().getSpeakerNotesShape().getText();let v=n.asString().replace(/^\d+:\d{2}[^\n]*\n*/,'').replace(/17:25 planned content[^\n]*|18:55 planned content[^\n]*/g,'');n.setText(fmt(dur[i])+' · '+fmt(elapsed)+'–'+fmt(elapsed+dur[i])+'\n\n'+v);elapsed+=dur[i];});
 old.forEach(s=>s.remove());console.log(JSON.stringify({count:out.length,duration:fmt(elapsed),slides:out.map((s,i)=>({n:i+1,id:s.getObjectId()}))}));
}
