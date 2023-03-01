**Build instructions**

1. Substrate

Get Substrate from https://github.com/johanvos/substrate/tree/entropy:

- Either clone it: `git clone --single-branch --branch entropy https://github.com/johanvos/substrate.git`
- or download it from https://github.com/johanvos/substrate/archive/refs/heads/entropy.zip and unzip it.

Build:

```
cd substrate
sh gradlew publishToMavenLocal
```

2. GluonFX plugin

Get the plugin from https://github.com/johanvos/gluonfx-maven-plugin/tree/staticlib:

- Either clone it: `git clone --single-branch --branch staticlib https://github.com/johanvos/gluonfx-maven-plugin.git`
- or download it from https://github.com/johanvos/gluonfx-maven-plugin/archive/refs/heads/staticlib.zip and unzip it.

Build:

```
cd gluonfx-maven-plugin
mvn clean install
```

3. GraalVM

Download and unzip from here: https://github.com/gluonhq/graal/releases/tag/gluon-22.1.0.1-Final

For M1 set:

`export GRAALVM_HOME=/path/to/graalvm-svm-java17-darwin-m1-gluon-22.1.0.1-Final/Contents/Home`

or for Intel set:

`export GRAALVM_HOME=/path/to/graalvm-svm-java17-darwin-gluon-22.1.0.1-Final/Contents/Home`

4. HelloFX

Clone this repo https://github.com/johanvos/gluon-samples/tree/JavaFXlet:

- Either clone it: `git clone --single-branch --branch JavaFXlet https://github.com/johanvos/gluon-samples.git`
- or download it from https://github.com/johanvos/gluonfx-maven-plugin/archive/refs/heads/staticlib.zip and unzip it.
https://github.com/johanvos/gluon-samples/archive/refs/heads/JavaFXlet.zip

Build static lib:

```
cd gluon-samples/HelloFX
mvn clean gluonfx:build gluonfx:staticlib
```

After a couple of minutes see that you get this file: `target/gluonfx/arm64-ios/gvm/libHelloFX.a`

5. Xcode

5.1 iOS Application

Create a new iOS Application in Xcode. For convenience, select Objetive-C language.

5.2 Header files

Add the generated header files from `target/gluonfx/arm64-ios/gvm/HelloFX/hellofx.hellofx.h` and `target/gluonfx/arm64-ios/gvm/HelloFX/graal_isolate.h` to the Xcode project.
For that you can drag and drop them from Finder, selecting "Copy items if needed".
Edit the `hellofx.hellofx.h` file, and replace `#include <graal_isolate.h>` with:

```
#include "graal_isolate.h"
```

5.3 Static library

Add the generated library from `target/gluonfx/arm64-ios/gvm/libHelloFX.a` to the Xcode project.
For that you can drag and drop it from Finder, selecting "Copy items if needed".

5.4 UIView

Let's add a simple button that you can use to trigger starting the embedded JavaFX app.

When clicking the button, it will invoke `startJavaFXApp` (see implementation in `localgl.m`).

Modify `ViewController.m` as follows:

```
#import "ViewController.h"
#import <GLKit/GLKit.h>

@interface ViewController ()

@end

extern void startJavaFXApp(void);

@implementation ViewController

- (void)graalThread:(id)argument {
    @autoreleasepool {
        NSLog(@"IN NEW THREAD");
        startJavaFXApp();
        NSLog(@"NEWCurrent thread = %@", [NSThread currentThread]);
        NSLog(@"NEWMain thread = %@", [NSThread mainThread]);
    }
}
- (void)myAction:(UIButton *)sender {
    NSLog(@"ACTION INVOKED!");
    NSLog(@"Current thread = %@", [NSThread currentThread]);
    NSLog(@"Main thread = %@", [NSThread mainThread]);
    [self performSelectorInBackground:@selector(graalThread:) withObject:(NULL)];
    NSLog(@"ACTION DONE!");
}

- (void)viewDidLoad {
    [super viewDidLoad];
    NSLog(@"Loading ViewController. Root window = %@", [[UIApplication sharedApplication].windows firstObject]);
    
    // Do any additional setup after loading the view.
    
    double mwidth = self.view.frame.size.width;
    
    CGRect frm0 = CGRectMake(0, 0, mwidth, 150);

    UIView *topView = [[UIView alloc] initWithFrame: frm0];
    topView.backgroundColor = [UIColor greenColor];

    CGRect br = CGRectMake(100, 100, 100, 30);
    UIButton *button = [[UIButton alloc] initWithFrame:br];
    [button setTitle:@"Click me" forState:UIControlStateNormal];
    [button addTarget:self action:@selector(myAction:) forControlEvents:UIControlEventTouchUpInside];
    [topView addSubview:button];
    [self.view addSubview:topView];
    
    // Just a dummy blue rect that will partially be hidden by the JavaFX app
    UIView *tmpView = [[UIView alloc] initWithFrame:CGRectMake(10, 200, 200, 100)];
    tmpView.backgroundColor = [UIColor blueColor];
    NSLog(@"Added topview to %@", self.view);
    NSLog(@"Current Thread: %@", [NSThread currentThread]);
    [self.view addSubview:tmpView];

    NSLog(@"Done!");
    NSLog(@"frame = %@", self.view);
}

@end
```

5.5 Helper file

Create a new .m file, name it `localgl.m`, and paste the following content:

```
#import <Foundation/Foundation.h>
#import "hellofx.hellofx.h"

extern int JNI_OnLoad_prism_es2(void);
extern int JNI_OnLoad_glass(void);
extern int JNI_OnLoad_javafx_font(void);
extern int JNI_OnLoad_nativeiio(void);
extern void Java_com_sun_prism_es2_IOSGLDrawable_nGetDummyDrawable(void);
extern void Java_com_sun_glass_ui_ios_IosWindow__1createWindow(void);
extern void Java_com_sun_glass_ui_ios_IosView__1create(void);
extern void Java_com_sun_glass_ui_ios_IosCursor__1set(void);
extern void Java_com_sun_glass_ui_ios_IosGestureSupport__1initIDs(void);

void loadlibs(void);
void startJavaFXApp(void);

void loadlibs() {
    JNI_OnLoad_prism_es2();
    JNI_OnLoad_glass();
    JNI_OnLoad_javafx_font();
    JNI_OnLoad_nativeiio();
    Java_com_sun_prism_es2_IOSGLDrawable_nGetDummyDrawable();
    Java_com_sun_glass_ui_ios_IosWindow__1createWindow();
    Java_com_sun_glass_ui_ios_IosView__1create();
    Java_com_sun_glass_ui_ios_IosCursor__1set();
    Java_com_sun_glass_ui_ios_IosGestureSupport__1initIDs();
}

/**
 Invoked by a click on the button. This will create a GraalVM thread and start the JavaFX app
 */
void startJavaFXApp() {
    graal_isolatethread_t *thread = NULL;
    int tres = graal_create_isolate(NULL, NULL, &thread);
    if (tres == 0) {
        fprintf(stderr, "Created thread!\n");
        fprintf(stderr, "thread = %p\n", thread);
        gluon_main(thread);
        fprintf(stderr, "MAIN DONE");
    } else {
        fprintf(stderr, "Couldn't create an isolate, tres = %d", tres);
    }
}
```

5.6 Static libs

Download this file https://download2.gluonhq.com/tmp/staticlibs.zip with static libraries, unzip it and add them to the Xcode project (via drag and drop from Finder).

In Xcode, go to Build Phases and see that you have 42 static libraries (some of then are not really needed). And press the `+` button to add `libz.1.2.11.tbd`.

6. Build and deploy to your iPhone

See that the app gets deployed, and pressing the button shows up the JavaFX fragment embedded into the UIView.





