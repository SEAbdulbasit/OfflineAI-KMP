/** @OnlyCurrentDoc */
function useOriginalLayouts(){
 const p=SlidesApp.getActivePresentation();if(p.getId()!=='13DefuAK4TPqDlWmC3ih0u-h6dqDOJkHpasNjHoWt0w4')throw new Error('Wrong deck');
 const all=p.getSlides();if(all.length!==23)throw new Error('Expected 20 slides + 3 templates');
 const old=all.slice(0,20), textTemplate=all[20],bulletTemplate=all[21],codeTemplate=all[22],out=[];
 function inherited(i,t,title,body){const s=p.appendSlide(t);const shapes=s.getShapes().sort((a,b)=>a.getTop()-b.getTop());shapes[0].getText().setText(title);if(body!==undefined){shapes[1].getText().setText(body);shapes[1].getText().getTextStyle().setFontFamily('Arial').setFontSize(18).setBold(false).setForegroundColor('#FFFFFF');}s.getNotesPage().getSpeakerNotesShape().getText().setText(old[i].getNotesPage().getSpeakerNotesShape().getText().asString());out.push(s);return s;}
 function keep(i,title){let s=p.appendSlide(old[i]);s.getShapes().filter(a=>a.getTop()===38).forEach(a=>a.remove());let h=s.insertShape(codeTemplate.getShapes()[0]);h.getText().setText(title);s.getShapes().forEach(a=>{if(a.getText().asString().trim()&&a.getText().getTextStyle().getFontFamily()!=='Roboto Mono')a.getText().getTextStyle().setFontFamily('Arial');});out.push(s);return s;}
 function code(i,title,v,z=17){let s=inherited(i,codeTemplate,title);s.getImages().forEach(a=>a.remove());let panel=s.insertShape(SlidesApp.ShapeType.ROUND_RECTANGLE,12,154.47834645669292,696,138.4724409448819);panel.getFill().setSolidFill('#F4F4F4');panel.getBorder().setTransparent();let a=s.insertTextBox(v,38,172,644,115);a.getText().getTextStyle().setFontFamily('Roboto Mono').setFontSize(z).setBold(false).setForegroundColor('#263238');a.getText().getParagraphStyle().setSpaceAbove(0).setSpaceBelow(0);a.getBorder().setTransparent();return s;}
 out.push(p.appendSlide(old[0]));
 inherited(1,textTemplate,'LLMs','Large language models learn patterns from training data.\nDuring inference, they generate text from the supplied context.\n\n“Turn on the flashlight” initially produces text—not a device action.');
 inherited(2,bulletTemplate,'Why Offline','Privacy → inference can keep prompts on the device\nAvailability → works offline after model setup\nCost → no per-request cloud inference bill\nLatency → no network round-trip; generation still takes time');
 inherited(3,bulletTemplate,'Mobile Limits','Start with one tool and a small set of requests\nSmall models can choose the wrong tool or arguments\nDo not assume reliable general reasoning or current facts\nEvaluate the exact model, prompt and device combination');
 inherited(4,textTemplate,'Tokens','Tokens are the pieces of text processed by a model.\nA token is not always a word; splitting depends on the tokenizer.\n\nThe context window must accommodate instructions, tools, conversation and output.');
 keep(5,'Weights & Quantization');
 keep(6,'Mobile AI');
 keep(7,'Gemma Models');
 keep(8,'LiteRT-LM');
 keep(9,'KMP');
 code(10,'Shared Inference API','expect class GemmaInference() {\n    suspend fun loadModel(modelPath: String,\n        config: ModelConfig = ModelConfig())\n    fun generateResponse(prompt: String): Flow<String>\n}');
 keep(11,'FunctionGemma');
 inherited(12,bulletTemplate,'Tool Definition','Name → toggle_torch\nDescription → turn the flashlight on or off\nParameter → enable, a required Boolean\nValidation → reject missing or extra arguments');
 code(13,'Function Calling','“Turn on the flashlight”\n\n<<{"tool":"toggle_torch",\n  "arguments":{"enable":true}}>>',20);
 out[out.length-1].getNotesPage().getSpeakerNotesShape().getText().appendText('\nExplain the execution sequence aloud: parse the proposed call, validate its arguments, execute through the native adapter, and report the native result.');
 inherited(14,bulletTemplate,'iOS','This project → LiteRT-LM through a Swift bridge\nSame shared prompt and parser as Android\nSwift SDK → early preview; also supports tools\nAlternative → Apple Foundation Models, when available');
 code(15,'System Prompt','To use a tool, reply with ONLY:\n<<{"tool":"TOOL_NAME","arguments":{...}}>>\nQ: Turn on flashlight\nA: <<{"tool":"toggle_torch","arguments":{"enable":true}}>>\nIf no tool needed, answer normally.',17);
 code(16,'Tool Validation','val value = arguments["enable"] as? JsonPrimitive\nval enable = value?.takeUnless { it.isString }\n    ?.booleanOrNull',18);
 inherited(17,bulletTemplate,'Demo Time','Prepare the model; disconnect the network\n“Turn on the flashlight” → inspect the proposed call\n“Turn it off” → verify the device response\nUnsupported request → check that no action is taken');
 inherited(18,bulletTemplate,'Evaluation','Correct tool name and valid arguments\nNo action for unsupported or malformed requests\nFirst-token latency and peak memory on each device\nPrompt and integration fixes before task-specific fine-tuning');
 keep(19,'Links');
 all.forEach(s=>s.remove());
 console.log(JSON.stringify({count:out.length,slides:out.map((s,i)=>({n:i+1,id:s.getObjectId()})),template:'Original imported text, bullet and code slide layouts'}));
}
