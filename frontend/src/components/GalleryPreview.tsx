interface Props {
    azukiId: number
    twitterHandle: string
    imageUrl: string
    modalImageUrl: string
    color: string
    bio: string | undefined
    hobbies: string[] | undefined
    openModal: (azukId: number, twitterHandle: string, color: string, imageUrl: string, bio: string | undefined, hobbies: string[] | undefined) => void
}

function GalleryPreview(props: Props) {
    function open(e: { preventDefault: () => void }) {
        e.preventDefault();

        props.openModal(props.azukiId, props.twitterHandle, props.color, props.modalImageUrl, props.bio, props.hobbies);
    }

    return (
        <a className="group cursor-pointer relative fade-in text-sm lg:-20 duration-300" onClick={open}>
            <div className="w-full relative fade-in lg:group-hover:scale-105 group-hover:shadow-me duration-300 rounded-xl square aspect-w-1 aspect-h-1 overflow-hidden bg-gray-100 shadow-me" style={{background: `rgb(161, 158, 153) none repeat scroll 0% 0%`}}>
                <div className="w-full h-full bg-white opacity-0 absolute z-50" />
                <div className="animate-flash-once w-full h-full bg-white opacity-0 absolute z-50" />
                <img className="duration-300 w-full h-full object-center object-cover absolute" src={props.imageUrl} alt={`Azuki #${props.azukiId}`} />
            </div>
            <p className="opacity-50 mt-3 uppercase font-mono tracking-widest text-3xs text-center">Azuki</p>
            <h3 className="font-400 pb-2 -mt-1 text-2xs tracking-wider text-center uppercase">No. {props.azukiId}</h3>
        </a>
    );
}

export default GalleryPreview;