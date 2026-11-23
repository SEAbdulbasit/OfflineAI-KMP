/** @OnlyCurrentDoc */
function buildConference() {
  const p = SlidesApp.getActivePresentation();
  if (p.getId() !== '13DefuAK4TPqDlWmC3ih0u-h6dqDOJkHpasNjHoWt0w4') throw new Error('Wrong presentation');
  const marker = '[Conference edition 2026-10-22]';
  const isDraft = s => s.getNotesPage().getSpeakerNotesShape().getText().asString().includes(marker);
  const original = p.getSlides().filter(s => !isDraft(s));
  if (original.length !== 23) throw new Error('Expected 23 source slides; refusing to rebuild a finished deck.');
  p.getSlides().filter(isDraft).forEach(s => s.remove());
  const portrait = original[0].getImages()[0];
  const demoImage = original[1].getImages()[0];
  const qr = original[21].getImages()[0];
  const C = {white:'#F4F7F5', accent:'#B7DBAD', muted:'#B8C9CE', panel:'#203E4F', dark:'#102632', line:'#45606C'};
  const out = [];
  function text(s, str, x, y, w, h, size=22, color=C.white, font='Google Sans', bold=false, align='START') {
    const sh = s.insertTextBox(str,x,y,w,h);
    sh.setContentAlignment(SlidesApp.ContentAlignment.TOP);
    const t = sh.getText();
    t.getTextStyle().setFontFamily(font).setFontSize(size).setBold(bold).setForegroundColor(color);
    t.getParagraphStyle().setParagraphAlignment(SlidesApp.ParagraphAlignment[align]).setSpaceAbove(0).setSpaceBelow(0);
    sh.getBorder().setTransparent();
    return sh;
  }
  function rect(s,x,y,w,h,color=C.panel) {
    const r=s.insertShape(SlidesApp.ShapeType.ROUND_RECTANGLE,x,y,w,h);
    r.getFill().setSolidFill(color); r.getBorder().setTransparent(); return r;
  }
  function line(s,x1,y1,x2,y2,color=C.line,arrow=false) {
    const l=s.insertLine(SlidesApp.LineCategory.STRAIGHT,x1,y1,x2,y2);
    l.getLineFill().setSolidFill(color); l.setWeight(1.5);
    if(arrow) l.setEndArrow(SlidesApp.ArrowStyle.FILL_ARROW);
    return l;
  }
  function notes(s, timing, prose, extras='') {
    s.getNotesPage().getSpeakerNotesShape().getText().setText(timing+'\n\n'+prose+'\n\n'+extras+'\n\n'+marker);
  }
  function slide(title, timing, prose, extras='',cover=false) {
    const s=p.appendSlide(cover?original[0]:original[2]);
    s.getPageElements().forEach(e=>e.remove());
    notes(s,timing,prose,extras);
    out.push(s);
    if(!cover) {
      text(s,title,54,34,612,47,30);
      line(s,60,94,660,94);
      text(s,String(out.length).padStart(2,'0'),635,375,30,18,10,C.muted,'Google Sans',false,'END');
    }
    return s;
  }
  function fitImage(s,img,x,y,w,h) {
    const copy=s.insertImage(img), scale=Math.min(w/copy.getWidth(),h/copy.getHeight());
    const iw=copy.getWidth()*scale, ih=copy.getHeight()*scale;
    copy.setWidth(iw).setHeight(ih).setLeft(x+(w-iw)/2).setTop(y+(h-ih)/2); return copy;
  }
  function boxText(s,str,x,y,w,h,size=22,fill=C.panel) {
    rect(s,x,y,w,h,fill); return text(s,str,x+10,y+12,w-20,h-20,size);
  }
  function pair(s,a,b,y) {
    boxText(s,a,60,y,240,56,22); boxText(s,b,420,y,240,56,22);
    line(s,320,y+28,398,y+28,C.accent,true);
  }
  let s=slide('', '0:30 · 0:00–0:30',
    'What can an AI feature still do when the phone loses its connection? We will follow OfflineAI-KMP from a local model to useful text tasks and a physical device action.',
    'Offline means the feature needs no network during the request after model and reference-data setup. This is a presentation of the current checkout; demo captures must be rehearsed on the presentation devices.',true);
  text(s,'Offline AI on Mobile\nwith Kotlin\nMultiplatform',40,37,440,145,34);
  text(s,'Useful AI, without a connection',48,194,440,35,20,C.accent);
  fitImage(s,portrait,58,252,62,62);
  text(s,'Abdul Basit',135,251,330,35,25);
  text(s,'Android GDE',136,286,300,27,17,C.muted);

  s=slide('Useful AI starts with everyday tasks','0:50 · 0:30–1:20',
    'Notes can become action items, and bundled documentation can support local answers. These bounded tasks keep working after setup without sending each inference request to a model server.',
    'OfflineAI-KMP implements note-to-action-item extraction and a bundled FAQ flow. The flashlight is a visible demonstration of execution, not the only useful application. Local inference still consumes memory, energy and time. A tool may introduce its own network dependency.');
  pair(s,'Meeting notes','Action items',122);
  pair(s,'Bundled documents','Answers + quotes',203);
  pair(s,'User request','Device action',284);

  s=slide('Inference uses learned weights','1:00 · 1:20–2:20',
    'An LLM is a model that generates language, and its weights are numbers learned during training. Inference uses those weights; Gemma is the model family and LiteRT-LM is the runtime.',
    'This app runs inference, not training. A prompt does not retrain the weights. Generated text can be plausible and still wrong.');
  text(s,'LLM = a model that generates language',60,112,600,35,22,C.muted);
  boxText(s,'Prompt',60,195,140,65,25);
  rect(s,257,169,207,116);
  text(s,'Gemma',277,182,167,37,27,C.accent);
  text(s,'Learned weights',277,226,170,30,19);
  boxText(s,'Output',520,195,140,65,25);
  line(s,212,228,245,228,C.accent,true); line(s,476,228,508,228,C.accent,true);
  text(s,'LiteRT-LM executes the model',185,315,350,35,22,C.white,'Google Sans',false,'CENTER');

  s=slide('Choose who manages the model','1:00 · 2:20–3:20',
    'Start with system models when their capabilities and supported devices meet your needs; manage your own model when you need a specific model or specialization. Both routes can support offline inference and tools, but model delivery and lifecycle ownership differ.',
    'Android system route: ML Kit GenAI APIs over Gemini Nano / AICore: https://developers.google.com/ml-kit/genai\nApple system route: on-device SystemLanguageModel through Foundation Models, with availability checks: https://developer.apple.com/documentation/foundationmodels\nApp-managed runtime examples beyond LiteRT-LM include llama.cpp, ExecuTorch and Core ML / MLX where supported; this talk follows LiteRT-LM. Offline AI also encompasses vision, speech and embeddings.');
  line(s,359,122,359,338);
  text(s,'System-managed',60,125,280,38,25,C.accent);
  text(s,'App-managed',390,125,270,38,25,C.accent);
  text(s,'Gemini Nano / AICore\nApple Foundation Models',60,188,285,85,21);
  text(s,'Your compatible model\nLiteRT-LM runtime',390,188,270,85,21);
  text(s,'Check availability',60,304,280,35,20,C.muted);
  text(s,'Own delivery + lifecycle',390,304,275,35,20,C.muted);

  s=slide('The phone sets the resource budget','1:20 · 3:20–4:40',
    'Weights are only part of RAM use: add context cache, runtime and application memory. Google reports Gemma 3 1B on a Samsung S24 Ultra at 33 CPU decode tokens/sec and 24 GPU decode tokens/sec; these are reference measurements, not results from this app.',
    'Weights-only arithmetic in decimal GB: 1 billion × 16 / 8 = 2 GB; 1 billion × 4 / 8 = 0.5 GB. Actual artifacts contain metadata and other data. Quantization can affect quality. Time to first token measures waiting before output, while decode throughput measures subsequent generation. Also measure complete-action latency, cold versus warm starts, peak RAM and thermal behavior. Source checked 22 October 2026: https://developers.google.com/edge/litert-lm/overview (Supported Models table, Gemma3-1B row). No TTFT number is claimed for this app.');
  text(s,'1B weights only',60,113,270,28,18,C.muted);
  text(s,'16-bit',60,155,100,32,22); rect(s,160,160,380,20,C.accent); text(s,'2 GB',560,151,95,40,25);
  text(s,'4-bit',60,204,100,32,22); rect(s,160,209,95,20,C.accent); text(s,'0.5 GB',275,200,150,40,25);
  line(s,60,264,660,264);
  text(s,'Load',60,279,140,35,22); text(s,'First token',265,279,170,35,22); text(s,'Generation',500,279,170,35,22);
  line(s,161,295,247,295,C.accent,true); line(s,420,295,482,295,C.accent,true);
  text(s,'Gemma 3 1B · S24 Ultra · CPU 33 / GPU 24 decode tok/s',60,337,600,26,16,C.muted);

  s=slide('New LLM work starts with LiteRT-LM','0:50 · 4:40–5:30',
    'Google has placed the MediaPipe LLM Inference API in maintenance-only mode and recommends migration to LiteRT-LM. This applies to the LLM Inference API, not to the entire MediaPipe project.',
    'Official notice: https://developers.google.com/edge/mediapipe/solutions/genai/llm_inference\nCurrent runtime: https://developers.google.com/edge/litert-lm/overview\nLiteRT-LM supports conversations, streaming and tool use with compatible models. Do not infer that every SDK feature is integrated in OfflineAI-KMP.');
  text(s,'MediaPipe\nLLM Inference',60,133,270,86,27);
  text(s,'LiteRT-LM',390,145,270,45,31,C.accent);
  line(s,323,180,367,180,C.accent,true);
  text(s,'Maintenance-only',60,255,280,38,24,C.muted);
  text(s,'Active development',390,255,280,38,24);
  text(s,'Use compatible .litertlm artifacts',60,334,600,30,19,C.muted);

  s=slide('KMP shares the behavior','1:10 · 5:30–6:40',
    'Compose UI, state, prompts and tool rules live in shared Kotlin. This real method from our expect contract returns a Flow of text chunks, while the actual implementations integrate native runtimes.',
    'Exact signature from composeApp/src/commonMain/kotlin/org/abma/offlinelai_kmp/inference/GemmaInference.kt:\nfun generateResponse(prompt: String): Flow<String>\nAndroid bridges LiteRT-LM Kotlin callbacks to Flow. iOS uses notifications to a Swift wrapper and returns streaming chunks to Kotlin. Chunks need not be individual tokens. Source: https://github.com/SEAbdulbasit/OfflineAI-KMP');
  rect(s,60,115,600,62);
  text(s,'Compose UI + state + prompts + tool rules',78,130,565,39,24);
  text(s,'fun generateResponse(prompt: String): Flow<String>',68,194,584,34,16,C.accent,'Roboto Mono');
  line(s,360,228,360,247); line(s,195,247,525,247); line(s,195,247,195,266,C.accent,true); line(s,525,247,525,266,C.accent,true);
  rect(s,60,277,270,75); rect(s,390,277,270,75);
  text(s,'Android actual',76,285,245,31,22);
  text(s,'Kotlin → LiteRT-LM',76,318,245,27,18,C.muted);
  text(s,'iOS actual',406,285,245,31,22);
  text(s,'Swift bridge → LiteRT-LM',406,318,250,27,17,C.muted);

  s=slide('Migration preserved shared callers','0:50 · 6:40–7:30',
    'We replaced the iOS MediaPipe bridge with LiteRT-LM while retaining the shared Kotlin interface. That isolates the native migration, but initialization, configuration and cleanup still differ between platforms.',
    'Repository evidence: migration commit 9d37458 (4 July 2026). Current Android initializes the engine on load and tries GPU then CPU; iOS initializes lazily on generation and uses the Swift bridge. Shared ModelConfig is not applied identically, and Kotlin close does not currently forward to the Swift bridge. No measured speed-up or RAM reduction is claimed.');
  boxText(s,'Shared GemmaInference contract',60,119,600,60,25);
  line(s,197,179,197,215); line(s,527,179,527,215);
  text(s,'BEFORE',60,213,260,28,14,C.muted); text(s,'AFTER',390,213,260,28,14,C.muted);
  boxText(s,'iOS / MediaPipe',60,249,270,60,24);
  boxText(s,'iOS / LiteRT-LM',390,249,270,60,24);
  line(s,341,279,378,279,C.accent,true);
  text(s,'Same callers. Native lifecycle still needs testing.',60,337,600,28,19,C.muted);

  s=slide('The model proposes. The app executes.','1:10 · 7:30–8:40',
    'Tool descriptions tell the model what actions are available, and it proposes a name and arguments. Our app parses the proposal, validates it in the handler, invokes the native action and returns the device result to chat.',
    'The proposal on this slide is an illustrative output in this app’s actual <<JSON>> format, not a captured inference result and not FunctionGemma’s universal wire format. The app does not automatically feed the tool result into another model turn. Structured extraction alone produces data; a calendar tool acting on the extracted fields executes an action. Calendar/contact creation are Google Mobile Actions reference capabilities, not claimed current OfflineAI-KMP tools.');
  text(s,'“Turn on the flashlight”',60,115,600,39,27);
  rect(s,60,173,600,102,C.dark);
  text(s,'<<{"tool":"toggle_torch",\n  "arguments":{"enable":true}}>>',77,188,570,74,20,C.accent,'Roboto Mono');
  text(s,'Example proposal',60,279,600,24,13,C.muted);
  boxText(s,'Validate',60,320,160,45,20); boxText(s,'Native action',280,320,160,45,20); boxText(s,'Result → UI',500,320,160,45,20);
  line(s,232,343,268,343,C.accent,true); line(s,452,343,488,343,C.accent,true);

  s=slide('Small models can specialize in actions','1:10 · 8:40–9:50',
    'Google’s Mobile Actions example fine-tunes FunctionGemma 270M for a bounded set of device tools and deploys it through LiteRT-LM. Its Colab workflow is a practical starting point: curate request/call pairs, include no-action cases, and evaluate unseen requests.',
    'Google reference application, not screenshots of OfflineAI-KMP. Kotlin API is stable: https://developers.google.com/edge/litert-lm/overview\nFunctionGemma is intended for task-specific fine-tuning, not direct general dialogue: https://ai.google.dev/gemma/docs/functiongemma/model_card\nOfficial tutorial + Colab: https://ai.google.dev/gemma/docs/mobile-actions\nImages by Google, used unchanged: https://ai.google.dev/gemma/docs/images/gallery-mobile-actions.png and https://ai.google.dev/gemma/docs/images/gallery-mobile-actions-finetune-challenge.png . Page content license: CC BY 4.0 unless otherwise noted. A small dataset is not a universal reliability guarantee. Fix prompt/template/integration failures before training.');
  text(s,'Android / LiteRT-LM',60,120,285,30,18,C.muted);
  text(s,'FunctionGemma',60,164,290,43,29);
  text(s,'270M parameters',60,210,290,40,27,C.accent);
  text(s,'Task-specific fine-tuning',60,269,300,31,20);
  const colab=text(s,'Official Colab workflow ↗',60,321,300,29,19,C.accent); colab.setLinkUrl('https://ai.google.dev/gemma/docs/mobile-actions');
  s.insertImage('https://ai.google.dev/gemma/docs/images/gallery-mobile-actions-finetune-challenge.png',375,111,136,244);
  s.insertImage('https://ai.google.dev/gemma/docs/images/gallery-mobile-actions.png',527,111,136,244);
  text(s,'Google AI Edge Gallery / Mobile Actions',365,357,310,22,11,C.muted,'Google Sans',false,'CENTER');

  s=slide('iOS has more than one route','0:55 · 9:50–10:45',
    'This project uses LiteRT-LM and the same shared prompt-and-parse flow as Android; Apple Foundation Models is an alternative, not our dependency. LiteRT-LM labels Swift early preview, and it also supports tools, so prompting is our implementation choice rather than an iOS limitation.',
    'API maturity, verified 22 October 2026: https://developers.google.com/edge/litert-lm/overview\nSwift tool use: https://developers.google.com/edge/litert-lm/swift\nApple’s on-device SystemLanguageModel requires availability checks: https://developer.apple.com/documentation/foundationmodels\nKotlin is listed Stable; Swift Early Preview. Test resource release, cancellation and configuration on each actual target.');
  line(s,360,122,360,341);
  text(s,'This project',60,128,270,38,26,C.accent); text(s,'System-model option',390,128,280,38,24,C.accent);
  text(s,'LiteRT-LM + Swift bridge\nShared prompt + parser',60,193,290,85,20);
  text(s,'Apple Foundation Models\nFramework tool calling',390,193,290,85,20);
  text(s,'Swift API: early preview',60,308,290,33,18,C.muted);
  text(s,'Check model availability',390,308,290,33,18,C.muted);

  s=slide('Our system prompt teaches the format','1:10 · 10:45–11:55',
    'These are exact selected lines from our generated system instructions, including a tool-call example and the no-tool rule. In this implementation the instructions are text prefixed to the conversation, not a separate privileged system-role API.',
    'Source: commonMain/tools/ToolPrompt.kt, buildSystemPrompt. This shared code is used on Android and iOS. The slide omits the tools list, parameter description, the Examples heading and the off example for space. Exact complete torch-only output:\n\nYou have access to these tools:\n\n- toggle_torch: Turn the device flashlight/torch on or off. Use this when the user wants to control the flashlight.\n  params: enable (true to turn on the torch, false to turn it off)\n\nTo use a tool, reply with ONLY:\n<<{"tool":"TOOL_NAME","arguments":{...}}>>\n\nExamples:\nQ: Turn on flashlight\nA: <<{"tool":"toggle_torch","arguments":{"enable":true}}>>\nQ: Turn off flashlight\nA: <<{"tool":"toggle_torch","arguments":{"enable":false}}>>\nUse JSON booleans true and false, not quoted strings or numbers.\n\nIf no tool needed, answer normally.\n\nCurrent iOS adapter: generateResponse(systemPrompt + currentPrompt). Check the selected model’s chat template; do not present this protocol as FunctionGemma’s training format.');
  rect(s,60,125,600,216,C.dark);
  text(s,'To use a tool, reply with ONLY:\n<<{"tool":"TOOL_NAME","arguments":{...}}>>\nQ: Turn on flashlight\nA: <<{"tool":"toggle_torch","arguments":{"enable":true}}>>\nIf no tool needed, answer normally.',76,153,568,170,15,C.accent,'Roboto Mono');

  s=slide('The app owns the execution boundary','1:00 · 11:55–12:55',
    'This is real Kotlin from the flashlight handler: a quoted string cannot become the Boolean argument. The following guard also rejects missing or extra keys, while native failures are reported separately from accepted requests.',
    'Exact source: commonMain/tools/Tooling.kt, ToggleTorchTool.execute.\nval value = arguments["enable"] as? JsonPrimitive\nval enable = value?.takeUnless { it.isString }?.booleanOrNull\nFollowing guard: if (arguments.keys != setOf("enable") || enable == null) returns an error without executing the action.\nThe general parser is lenient and accepts several formats; do not call it a strict parser. Registry lookup rejects unknown tool names. Native acceptance is not independent physical verification that the flashlight changed.');
  rect(s,60,126,600,110,C.dark);
  text(s,'val value = arguments["enable"] as? JsonPrimitive\nval enable = value?.takeUnless { it.isString }?.booleanOrNull',74,151,575,68,15,C.accent,'Roboto Mono');
  text(s,'{"enable": true}',60,263,285,35,20,C.white,'Roboto Mono'); text(s,'{"enable": "true"}',390,263,285,35,20,C.white,'Roboto Mono');
  text(s,'Eligible to execute',60,309,285,31,20,C.accent); text(s,'Rejected',390,309,285,31,20,C.muted);

  s=slide('Useful output, then a real action','3:00 · 12:55–15:55',
    'First we will extract action items from a note, then ask the app to turn the flashlight on and off with the network disconnected. We will inspect model output and native execution separately, including what happens when a request fails.',
    'DEMO PLACEHOLDER: insert only an actual rehearsed recording if using a recorded demo. The images on this slide are existing iOS simulator input screens with no model loaded, not generated results. No successful physical-device inference or torch operation is asserted by these captures.\n3-minute run: 20 sec model ready / airplane mode / Wi-Fi off; 60 sec note extraction; 80 sec flashlight on/off plus proposal and native result; 20 sec observed limitation. Prepare a fallback recording from the exact demo build and identify device, model, quantization and backend.');
  text(s,'Model ready / airplane mode / Wi-Fi off',60,111,600,30,19,C.accent);
  text(s,'01\nNotes → action items',60,174,270,76,25);
  text(s,'02\nFlashlight on / off',60,267,270,76,25);
  fitImage(s,demoImage,335,170,325,184);
  text(s,'Actual iOS input screens',340,355,315,23,12,C.muted,'Google Sans',false,'CENTER');

  s=slide('Start with one task you can evaluate','0:50 · 15:55–16:45',
    'Choose a task where you can recognize a correct result and explain a failure. Share behavior through KMP, then measure correctness, latency, memory and native execution on both physical devices.',
    'Examples are product patterns, not universal guarantees: extract task/owner/deadline from notes; answer from bundled FAQ text with a supporting quote; execute an allowed device action. Test unsupported requests and missing information.');
  text(s,'Meeting notes → action items',60,131,600,43,28);
  text(s,'Bundled FAQ → answer + source quote',60,208,600,43,27);
  text(s,'Request → validated device action',60,285,600,43,27);
  line(s,60,348,660,348);
  text(s,'Correctness / latency / peak RAM',60,357,600,24,16,C.accent);

  s=slide('Your next experiment is one bounded tool','0:40 · 16:45–17:25',
    'Explore the shared and platform source sets, then choose a compatible model and one target feature. Add one tool and test successful requests, malformed arguments and requests that should perform no action.',
    'Repository: https://github.com/SEAbdulbasit/OfflineAI-KMP\nLiteRT-LM: https://developers.google.com/edge/litert-lm/overview\nKotlin: https://developers.google.com/edge/litert-lm/android\nSwift: https://developers.google.com/edge/litert-lm/swift\nFunctionGemma model card: https://ai.google.dev/gemma/docs/functiongemma/model_card\nMobile Actions + Colab + Google imagery: https://ai.google.dev/gemma/docs/mobile-actions\nSystem models: https://developers.google.com/ml-kit/genai and https://developer.apple.com/documentation/foundationmodels\n17:25 planned content including the 3-minute demo; reserve roughly 1–2 minutes for transitions and questions.');
  fitImage(s,qr,60,125,190,190).setLinkUrl('https://github.com/SEAbdulbasit/OfflineAI-KMP');
  const links=[['OfflineAI-KMP','https://github.com/SEAbdulbasit/OfflineAI-KMP'],['LiteRT-LM / Kotlin + Swift','https://developers.google.com/edge/litert-lm/overview'],['FunctionGemma / Mobile Actions','https://ai.google.dev/gemma/docs/mobile-actions'],['Android system models','https://developers.google.com/ml-kit/genai']];
  links.forEach((v,i)=>text(s,v[0],285,129+i*49,385,35,20,i===0?C.accent:C.white).setLinkUrl(v[1]));
  text(s,'One tool. Five requests. Two devices.',60,345,600,35,24,C.accent);
  original.forEach(s=>s.remove());
  console.log(JSON.stringify({result:'complete',count:out.length,slides:out.map((s,i)=>({n:i+1,id:s.getObjectId()}))}));
}

function auditConference() {
  const p=SlidesApp.getActivePresentation();
  console.log(JSON.stringify({count:p.getSlides().length,width:p.getPageWidth(),height:p.getPageHeight()}));
  p.getSlides().forEach((s,i)=>{
    const errors=[];
    s.getPageElements().forEach(e=>{if(e.getLeft() < -1 || e.getTop() < -1 || e.getLeft()+e.getWidth()>721 || e.getTop()+e.getHeight()>406) errors.push(e.getObjectId());});
    console.log(JSON.stringify({slide:i+1,id:s.getObjectId(),images:s.getImages().length,notes:s.getNotesPage().getSpeakerNotesShape().getText().asString().length,offCanvas:errors,text:s.getShapes().map(x=>x.getText().asString()).join(' / ')}));
  });
}
