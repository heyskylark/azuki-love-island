import serialToImageNameJson from "../models/image/ImageToUUIDMapping";

interface Props {
    azukiId: number;
    twitterHandle: string;
    vs?: boolean;
    className?: string;
}

function SmallImagePreview(props: Props) {
    function getPreviewUrl(): string {
        // @ts-ignore
        const uuid = serialToImageNameJson[props.azukiId]
        return `https://azk.imgix.net/images/${uuid}.png?fm=jpg&w=800`
    }

    function parseTwitterHandle(): string {
        const handle = props.twitterHandle;

        if (handle.startsWith("@")) {
            return handle
        } else if (handle.startsWith("https://") || handle.startsWith("http://") || handle.startsWith("www") || handle.startsWith("twitter.com")) {
            const split = handle.split("/")
            return `@${split[split.length - 1]}`;
        } else {
            return `@${handle}`;
        }
    }

    function renderVs() {
        if (props.vs) {
            return <img className="absolute w-1/3 top-[30%] left-[83%] z-[37]" src="/images/vs.png" alt='' />;
        }
    }

    return (
        <div className={`${props.className} group relative text-sm lg:-20`}>
            {renderVs()}
            <div className="w-full relative rounded-xl square aspect-w-1 aspect-h-1 overflow-hidden bg-gray-100 shadow-me" style={{background: `rgb(161, 158, 153) none repeat scroll 0% 0%`}}>
                <div className="w-full h-full bg-white opacity-0 absolute z-[36]" />
                <div className="animate-flash-once w-full h-full bg-white opacity-0 absolute z-[36]" />
                <img className="w-full h-full object-center object-cover absolute" src={getPreviewUrl()} alt={`Azuki #${props.azukiId}`} />
            </div>

            <p className="opacity-50 mt-2 font-mono tracking-widest text-3xs text-center">{parseTwitterHandle()}</p>
            <h3 className="font-400 pb-2 -mt-1 text-2xs tracking-wider text-center uppercase">No. {props.azukiId}</h3>
        </div>
    );
}

export default SmallImagePreview;
